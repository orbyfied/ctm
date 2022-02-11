package com.github.orbyfied.argument;

import java.util.StringJoiner;

public class Raw {

    private String str;

    public Raw(String str) {
        this.str = str;
    }

    public String getString() {
        return str;
    }

    public void setString(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
