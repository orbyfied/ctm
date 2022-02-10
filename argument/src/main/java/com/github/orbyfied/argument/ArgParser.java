package com.github.orbyfied.argument;

import com.github.orbyfied.util.StringIterator;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

public class ArgParser {

    public static final Object FAILED = new Object();

    private final List<ArgOption> args = new ArrayList<>();
    private final Map<String, ArgOption> longArgToOption = new HashMap<>();
    private final Map<Character, ArgOption> shortArgToOption = new HashMap<>();
    private final List<ArgOption> unnamedArgs = new ArrayList<>();

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

    @SuppressWarnings("unchecked")
    public ArgParser parse(String str, Map<String, Object> res) {
        ConsumingUpper consumingUpper = new ConsumingUpper(res);
        this.str = str;
        argindex = 0;
        iter = new StringIterator(str, -1);
        char c;
        while ((c = iter.next()) != StringIterator.DONE) {
//            /* DEBUG */ System.out.println("C: " + c + ", cur: " + res);
            int i1 = iter.index();
            if (c == '-') { // is named argument
                if (iter.peek(1) == '-') { // is long argument
                    iter.next(2);
                    String arg = collectLongArg();
                    ArgOption option = longArgToOption.get(arg);
                    if (option == null)
                        erriag("Unresolved option: " + arg + " does not exist as a long option", i1, iter.index());
                    if (iter.current() == ' ') {
                        if (option.getType().getLower() == Boolean.class) {
                            res.put(option.getName(), true);
                            continue;
                        } else {
                            erriag("Syntax error: missing operator after specification of " +
                                    option.getName(), i1, iter.index());
                        }
                    }

                    String operator = collectOperator();
                    String valstr = collectOptValueStr();

                    ArgType<?, ?> type = option.getType();
                    Object val = type.convert(valstr);
                    if (type.getUpper() == ConsumingUpper.class)
                        ((ArgType<ConsumingUpper, Object>)type).apply(consumingUpper.re(arg), val, operator);
                    else if (type.getUpper() == List.class) {
                        List list = (List)res.get(arg);
                        if (list == null) list = (List)res.computeIfAbsent(arg, arg1 -> new ArrayList<>());
                        ((ArgType<List, Object>)type).apply(list, val, operator);
                    }
                } else { // is short argument(s)
                    parseShortArgs(res);
                }
            } else {
                if (c == ' ' || c == '\n' || c == '\t') continue;
                if (argindex >= unnamedArgs.size())
                    erriag("Unresolved option: (unnamed) index " + argindex, i1, iter.index());
                ArgOption option = unnamedArgs.get(argindex);
                String valstr = collectOptValueStr();

                ArgType<?, ?> type = option.getType();
                Object val = type.convert(valstr);
                if (type.getUpper() == ConsumingUpper.class)
                    ((ArgType<ConsumingUpper, Object>)type).apply(consumingUpper.re(option.getName()), val, "=");

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

    private String collectOperator() {
        return iter.collect(ArgParser::isValidOperatorChar);
    }

    private String collectOptValueStr() {
        if (iter.current() == '"' || iter.current() == '\'') return collectStr(iter.current());
        return collectSpaceTermStr();
    }

    private String collectStr(char startChar) {
        iter.next();
        String s = iter.collect(c -> c != startChar);
        iter.next();
        return s;
    }

    private String collectSpaceTermStr() {
        return iter.collect(c -> c != ' ');
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
        return ((c >= 33 && c <= 47) || (c >= 58 && c <= 63)) && (c != '"' && c != '\'');
    }

}
