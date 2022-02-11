package com.github.orbyfied.argument;

import com.github.orbyfied.util.StringIterator;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ArgParser {

    public static final Object FAILED = new Object();

    private final List<ArgOption> args = new ArrayList<>();
    private final Map<String, ArgOption> longArgToOption = new HashMap<>();
    private final Map<Character, ArgOption> shortArgToOption = new HashMap<>();
    private final List<ArgOption> unnamedArgs = new ArrayList<>();

    private Consumer<String> warningHandler;
    private BiFunction<Class<?>, String, Object> valParser;

    public ArgParser withValueParser(BiFunction<Class<?>, String, Object> f) {
        this.valParser = valParser;
        return this;
    }

    public ArgParser withOption(ArgOption option) {
        this.args.add(option);
        if (option.isNamed()) {
            longArgToOption.put(option.getName(), option);
            for (String a : option.aliases())
                longArgToOption.put(a, option);
            if (option.hasShort()) {
                if (option.getType().getLower() != Boolean.class)
                    throw new IllegalArgumentException("short option can only be a switch");
                for (char c : option.shorts())
                    shortArgToOption.put(c, option);
            }
        } else {
            unnamedArgs.add(option);
        }
        return this;
    }

    public ArgParser withWarningHandler(Consumer<String> consumer) {
        this.warningHandler = consumer;
        return this;
    }

    public ArgParser withOptions(ArgOption... options) {
        for (ArgOption option : options)
            withOption(option);
        return this;
    }

    StringIterator iter;
    int argindex = 0;
    String str;

    private void erriag(String err, int posStart, int posEnd) {
        try {
            String errposstr =
                    (posStart == -1) ?
                            "unknown"
                            :
                    posStart + (posEnd != -1 ? "-" + posEnd : "") + ": '" +
                    str.substring(Math.max(str.length() - 1, posStart), Math.min(str.length() - 1, posEnd != -1 ? posEnd : posStart + 10))
                    + "'";
            throw new IllegalArgumentException(err + " <-- " + errposstr + " (C: '" + iter.current() + "', I: " + iter.index() + ")");
        } catch (Exception e) {
            throw new IllegalArgumentException(err + " <-- " + " (C: '" + iter.current() + "', I: " + iter.index() + ")");
        }
    }

    private void warnf(String s) {
        if (warningHandler != null)
            warningHandler.accept(s);
    }

    public ArgParser parse(String str, Map<String, Object> res) {
        return parse(str, new ArgContext(res));
    }

    @SuppressWarnings("unchecked")
    public ArgParser parse(String str, ArgContext context) {
        // construct context elements
        Map<String, Object> res = context.getSymbols();
        ConsumingUpper consumingUpper = new ConsumingUpper(res);

        // set values and create iterator
        this.str = str;
        argindex = 0;
        iter = new StringIterator(str, -1);

        // iterate
        char c;
        while ((c = iter.next()) != StringIterator.DONE) {
//            /* DEBUG */ System.out.println("C: " + c + ", cur: " + res);
            // get start index for error debugging
            int i1 = iter.index();
            if (c == '-') { // is named argument
                if (iter.peek(1) == '-') { // is long argument
                    // collect and get option
                    iter.next(2);
                    String arg = collectLongArg();
                    ArgOption option = longArgToOption.get(arg);

                    // check if the option exists
                    if (option == null)
                        erriag("Unresolved option: " + arg + " does not exist as a long option", i1, iter.index());

                    // check if it even has an operator
                    if (iter.current() == ' ') {
                        // if it is a switch, turn it on
                        if (option.getType().getLower() == Boolean.class) {
                            res.put(option.getName(), true);
                            continue;
                        } else {
                            // throw exception
                            erriag("Syntax error: missing operator after specification of " +
                                    option.getName(), i1, iter.index());
                        }
                    }

                    // collect operator
                    String operator = collectOperator();

                    // get type and value
                    ArgType<?, ?> type = option.getType();
                    Object val;
                    boolean isVar = false;
                    if (iter.current() == '$') {
                        val = getOrInvokeSymbol(context);
                        isVar = true;
                    } else {
                        val = type.parse(collectOptValueStr(context));
                    }

                        // parse raw string if it is one
                    if (val instanceof Raw)
                        val = type.parse(((Raw)val).getString());
                    else if (isVar)
                        // parse string if it is a different type
                        // basically casting it
                        if (!type.accepts(val.getClass()))
                            val = type.parse(Objects.toString(val));

                    // apply
                    if (type.getUpper() == ConsumingUpper.class)
                        ((ArgType<ConsumingUpper, Object>) type).apply(consumingUpper.re(arg), val, operator);
                    else if (type.getUpper() == List.class) {
                        List list = (List) res.get(arg);
                        if (list == null) list = (List) res.computeIfAbsent(arg, arg1 -> new ArrayList<>());
                        ((ArgType<List, Object>) type).apply(list, val, operator);
                    }
                } else { // is short argument(s)
                    parseShortArgs(res);
                }
            } else if (c == '$') { // is variable declaration
                // collect variable name
                iter.next();
                String name = collectLongArg();

                // check operator
                if (iter.current() != '=')
                    erriag("Syntax error: expected = after variable assignment", i1, iter.index());
                iter.next();

                // collect and parse value
                Object val;
                if (iter.current() == '$') {
                    val = getOrInvokeSymbol(context);
                } else {
                    val = ArgType.GENERAL.parse(collectOptValueStr(context));
                }

                // set
                res.put("$" + name, val);
            } else { // is unnamed
                // check if it is not whitespace
                if (c == ' ' || c == '\n' || c == '\t') continue;

                // check if another unnamed argument exists
                if (argindex >= unnamedArgs.size())
                    erriag("Unresolved option: (unnamed) index " + argindex, i1, iter.index());

                // get option
                ArgOption option = unnamedArgs.get(argindex);

                ArgType<?, ?> type = option.getType();
                Object val;
                boolean isVar = false;
                if (iter.current() == '$') {
                    val = getOrInvokeSymbol(context);
                    isVar = true;
                } else {
                    val = type.parse(collectOptValueStr(context));
                }

                if (val instanceof Raw)
                    val = type.parse(((Raw)val).getString());
                else if (isVar)
                    // parse string if it is a different type
                    // basically casting it
                    if (!type.accepts(val.getClass()))
                        val = type.parse(Objects.toString(val));

                // apply
                if (type.getUpper() == ConsumingUpper.class)
                    ((ArgType<ConsumingUpper, Object>)type).apply(consumingUpper.re(option.getName()), val, "=");

                // advance unnamed index
                argindex++;
            }
        }
        check(res);
        return this;
    }

    private void check(Map<String, Object> map) {
//        /* DEBUG */ System.out.println("final: " + map);
        for (ArgOption option : args) {
            String n = option.getName();
            boolean has = map.containsKey(n);
            if (!has && option.isRequired()) {
                erriag("missing required parameter " + option.getName(), -1, -1);
            } else if (!has) {
                if (option.getDefaultValue() != null)
                    map.put(n, option.getDefaultValue());
                else if (option.getType().getLower() == Boolean.class)
                    map.put(n, false);
            }
        }
    }

    private Object getOrInvokeSymbol(ArgContext context) {
        Map<String, Object> sym = context.getSymbols();
        if (iter.next() != '{')
            erriag("Syntax error: expected { to ref variable", iter.index(), iter.index());
        int i1 = iter.index();
        StringBuilder nameb = new StringBuilder();
        char c;
        boolean call = false;
        List<Object> arguments = null;
        while ((c = iter.next()) != StringIterator.DONE) {
            if (c == ' ') continue;
            if (c == '}') break;
            if (c == '(') {
                call = true;
                arguments = new ArrayList<>(2);
                StringBuilder lastArg = null;
                iter.prev();
                while ((c = iter.next()) != StringIterator.DONE) {
//                    System.out.println(c);
                    if (c == ' ') continue;
                    if (c == '(' || c == ',') {
                        if (lastArg != null)
                            arguments.add(ArgType.GENERAL.parse(lastArg.toString()));
                        lastArg = new StringBuilder();
                        while ((c = iter.next()) != StringIterator.DONE) {
                            if (c == '$') {
                                arguments.add(getOrInvokeSymbol(context));
                                iter.next();
                                continue;
                            }
                            if (c != ' ')
                                break;
                        }
                    }
                    if (c == ',' || c == ')') {
                        String las = lastArg.toString();
                        Object val;
                        val = ArgType.GENERAL.parse(las);
                        arguments.add(val);
                        lastArg = new StringBuilder();
                        if (c == ')') break;
                        continue;
                    }
                    lastArg.append(c);
                }
                continue;
            }
            nameb.append(c);
        }
        String name = nameb.toString();
        String fn = "$" + name;
        if (!sym.containsKey(fn))
            warnf("Unresolved variable: " + fn);
        if (!call)
            return sym.get(fn);
        else {
            Object v = sym.get(fn);
            if (!(v instanceof ArgFunction))
                erriag("attempt to call non-function " + fn, i1, iter.index());
            return ((ArgFunction)v).invoke(context, arguments.toArray());
        }
    }

    private String collectOperator() {
        return iter.collect(ArgParser::isValidOperatorChar);
    }

    private String collectOptValueStr(ArgContext ctx) {
        if (iter.current() == '$') {
            return Objects.toString(getOrInvokeSymbol(ctx));
        }
        if (iter.current() == '"' || iter.current() == '\'') return collectStr(iter.current(), ctx);
        return collectComplex(' ', ctx);
    }

    private String collectStr(char startChar, ArgContext ctx) {
        iter.next();
        String s = collectComplex(startChar, ctx);
        iter.next();
        return s;
    }

    private String collectComplex(char matchChar, ArgContext ctx) {
        StringBuilder b = new StringBuilder();
        iter.prev();
        char c;
        while ((c = iter.next()) != StringIterator.DONE) {
            if (c == matchChar) break;
            if (c == '\\') {
                b.append(iter.next());
            } else if (c == '$') {
                Object o = getOrInvokeSymbol(ctx);
                if (o instanceof Raw)
                    b.append(((Raw)o).getString());
                else b.append(o);
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    private void parseShortArgs(Map<String, Object> map) {
        iter.next();
        String s = iter.collect(ArgParser::isValidShortArgChar);
        for (int i = 0; i < s.length(); i++) {
            ArgOption opt = shortArgToOption.get(s.charAt(i));
            if (opt == null)
                throw new IllegalArgumentException("Unresolved option: (short) " + s.charAt(i));
            map.put(opt.getName(), true);
        }
    }

    private String collectLongArg() {
        return iter.collect(ArgParser::isValidArgChar);
    }

    private static String toStringArrNb(Iterable<?> arr) {
        StringBuilder b = new StringBuilder();
        for (Object o : arr)
            b.append(o).append(", ");
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }

    private static boolean isValidArgChar(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z')
                || c == '-' || c == '_';
    }

    private static boolean isValidShortArgChar(char c) {
        return (c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z');
    }

    private static boolean isValidOperatorChar(char c) {
        return ((c >= 33 && c <= 47) || (c >= 58 && c <= 63)) && (c != '"' && c != '\'' && c != '$');
    }

}
