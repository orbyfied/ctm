package com.github.orbyfied.argument;

import java.util.Map;

public class ConsumingUpper {

    Map<String, Object> res;
    String name;

    public ConsumingUpper(Map<String, Object> res) {
        this.res = res;
    }

    public ConsumingUpper re(String name) {
        this.name = name;
        return this;
    }

    public void push(Object o) {
        res.put(name, o);
    }

}
