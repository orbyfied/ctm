package com.github.orbyfied.argument;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class Args {

    private Map<String, Object> result = new HashMap<>();

    public Args parse(String s, Consumer<ArgParser> consumer) {
        ArgParser parser = new ArgParser();
        consumer.accept(parser);
        parser.parse(s, result);
        return this;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public <T> T get(String key) {
        return (T)result.get(key);
    }

    public <T> T get(String key, Class<T> tClass) {
        return (T)result.get(key);
    }

    public <T> Optional<T> getOptional(String key) {
        return Optional.of((T)result.get(key));
    }

}
