package com.github.orbyfied.argument;

import com.github.orbyfied.util.StringIterator;
import com.github.orbyfied.util.TriConsumer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

public class ArgType<U, L> {

    /**
     * Class of the upper type (e.g. List)
     */
    private Class<U> upper;

    /**
     * Class of the value type (e.g. Match or in a map Entry)
     */
    private Class<L> lower;

    /**
     * String -> Lower
     */
    private Function<String, Object> parser;

    /**
     * Upper, Lower & Operator
     */
    private TriConsumer<U, Object, String> applier;

    public ArgType(
            Class<U> upper,
            Class<L> lower,
            Function<String, Object> parser,
            TriConsumer<U, Object, String> applier
    ) {
        this.upper = upper;
        this.lower = lower;

        this.parser = parser;
        this.applier   = applier;
    }

    public Class<U> getUpper() {
        return upper;
    }

    public Class<L> getLower() {
        return lower;
    }

    public Object parse(String str) {
        return parser.apply(str);
    }

    public void apply(U upper, L lower, String operator) {
//        /* DEBUG */ System.out.println("U: " + upper + ", L: " + lower + ", OP: " + operator);
        applier.accept(upper, lower, operator);
    }

    public boolean accepts(Class<?> klass) {
        return lower.isAssignableFrom(klass);
    }

    @Override
    public String toString() {
        return "ArgType(" + upper + ":" + lower + ")";
    }

    /////////////////////////////////////////

    public static <T> ArgType<ConsumingUpper, T> mono(final Class<T> tClass) {
        return new ArgType<>(ConsumingUpper.class, tClass,
                s -> parseValue(s, tClass),
                (upper, t, s) -> {
            if (s.equals("=")) upper.push(t);
        });
    }

    public static <T> ArgType<ConsumingUpper, T> mono(final Class<T> tClass, Function<String, Object> parser) {
        return new ArgType<>(ConsumingUpper.class, tClass, parser, (upper, t, s) -> {
            if (s.equals("=")) upper.push(t);
        });
    }

    @SuppressWarnings({"unchecked"})
    public static <T> ArgType<List, T> listing(final Class<T> tClass) {
        return new ArgType<>(List.class, tClass,
                s -> parseValue(s, tClass),
                (upper, t, s) -> {
                    switch (s) {
                        case "+=" -> upper.add(t);
                        case "-=" -> upper.remove(t);
                        case "="  -> upper.addAll((List)t);
                    }
                });
    }

    @SuppressWarnings({"unchecked"})
    public static <T> ArgType<List, T> listing(final Class<T> tClass, Function<String, Object> parser) {
        return new ArgType<>(List.class, tClass, parser, (upper, t, s) -> {
                    switch (s) {
                        case "+=" -> upper.add(t);
                        case "-=" -> upper.remove(t);
                        case "="  -> upper.addAll((List)t);
                    }
                });
    }

    public static final ArgType<ConsumingUpper, Object> GENERAL = new ArgType<ConsumingUpper, Object>(
            ConsumingUpper.class, Object.class, ArgType::parseValueAn,
                (upper1, o, s) -> upper1.push(o)
    );

    /////////////////////////////////////////

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static Object parseValueAn(String s) {
        if (s.startsWith("\"")) {
            return s.substring(1, s.length() - 1);
        } else if (isDigit(s.charAt(0))) {
            StringIterator iter = new StringIterator(s, -1);
            char c;
            StringBuilder b = new StringBuilder();
            boolean hasDecimals = false;
            while ((c = iter.next()) != StringIterator.DONE) {
                if (c == ' ' || c == '_') continue;
                if (c == '.' && !hasDecimals) hasDecimals = true;
                if (!isDigit(c)) break;
                b.append(c);
            }
            return hasDecimals ? Double.parseDouble(b.toString()) : Long.parseLong(b.toString());
        } else return new Raw(s);
    }

    private static Object parseValue(String s, Class<?> klass) {
        if (s.startsWith("[")) return parseList(s, klass);
        if (klass == String.class) return s;
        else if (klass == Integer.class) return Integer.parseInt(s);
        else if (klass == Long.class) return Long.parseLong(s);
        else if (klass == Double.class) return Double.parseDouble(s);
        else if (klass == Float.class) return Float.parseFloat(s);
        else if (klass == Character.class) return s.charAt(0);
        else if (klass == Boolean.class) return Boolean.parseBoolean(s);
        else if (klass == Path.class) return Path.of(s);
        else if (klass == File.class) return new File(s);
        return null;
    }

    private static Object parseList(String s, Class<?> klass) {
        StringIterator iterator = new StringIterator(s, 0);
        char c;
        StringBuilder b = new StringBuilder();
        List<Object> list = new ArrayList<>();
        while ((c = iterator.next()) != StringIterator.DONE && c != ']') {
            if (c == ' ' || c == '\n' || c == '\t') continue;
            if (c == ',') { // push
                list.add(parseValue(b.toString(), klass));
                b = new StringBuilder();
            } else if (c == '"' || c == '\'') {
                b.append(collectStr(iterator));
            } else {
                b.append(c);
            }
        }
        if (!b.isEmpty())
            list.add(parseValue(b.toString(), klass));

        return list;
    }

    private static String collectStr(StringIterator iter) {
        iter.next();
        return iter.collect(c -> c != '"' && c != '\'');
    }

}
