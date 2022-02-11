package com.github.orbyfied.argument;

import java.util.HashMap;
import java.util.Map;

public class ArgContext {

    public ArgContext(Map<String, Object> symbols) {
        this.symbols = symbols;
    }

    public ArgContext() {
        this.symbols = new HashMap<>();
    }

    private Map<String, Object> symbols;

    public Map<String, Object> getSymbols() {
        return symbols;
    }

    public void setSymbolValue(String key, Object val) {
        this.symbols.put(key, val);
    }

    public void setVariableValue(String key, Object val) {
        this.symbols.put("$" + key, val);
    }

    public void setFunctionValue(String key, Object func) {
        this.symbols.put("$" + key, new ArgFunction(func));
    }

    @SuppressWarnings("unchecked")
    public <T> T getSymbolValue(String key) {
        return (T) symbols.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSymbolValue(String key, Class<T> tClass) {
        return (T) symbols.get(key);
    }

}
