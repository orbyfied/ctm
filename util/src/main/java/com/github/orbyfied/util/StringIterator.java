package com.github.orbyfied.util;

import java.util.function.Predicate;

public class StringIterator {

    public static final char DONE = '\uFFFF';

    private int index = 0;
    private final String str;
    private final int len;

    public StringIterator(String str, int index) {
        this.str   = str;
        this.len   = str.length();
        this.index = index;
    }

    public int clamp(int index) {
        return Math.min(len - 1, Math.max(0, index));
    }

    public char peekAt(int i) {
        return str.charAt(clamp(i));
    }

    public char peek(int i) {
        return str.charAt(clamp(index + i));
    }

    public char next() {
        if (index >= len - 1) return DONE;
        return str.charAt(clamp(index += 1));
    }

    public char next(int a) {
        if (index >= len - 1) return DONE;
        return str.charAt(clamp(index += a));
    }

    public char prev() {
        return str.charAt(clamp(index -= 1));
    }

    public char prev(int a) {
        return str.charAt(clamp(index -= a));
    }

    public char current() {
        if (len == 0) return DONE;
        return str.charAt(clamp(index));
    }

    public String collect(Predicate<Character> pred) {
        StringBuilder b = new StringBuilder();
        prev();
        char c;
        while ((c = next()) != DONE && pred.test(c))
            b.append(c);
        return b.toString();
    }

    public int index() {
        return index;
    }

    public StringIterator index(int i) {
        this.index = i;
        return this;
    }

    public String getString() {
        return str;
    }

}
