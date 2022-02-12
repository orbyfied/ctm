package com.github.orbyfied.argument;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class Args {

    private ArgContext context;
    private Map<String, Object> results;

    public Args() {
        this(new ArgContext());
    }

    public Args(ArgContext context) {
        Objects.requireNonNull(context, "context cannot be null");
        this.context = context;
        this.results = context.getResults();
        StdContext.apply(context);
    }

    public Args parse(String s, Consumer<ArgParser> consumer) {
        ArgParser parser = new ArgParser();
        if (consumer != null)
            consumer.accept(parser);
        parser.parse(s, context);
        results = context.getResults();
        return this;
    }

    public Map<String, Object> getResult() {
        return results;
    }

    public Map<String, Object> getSymbols() {
        return context.getSymbols();
    }

    public ArgContext getContext() {
        return context;
    }

    public <T> T get(String key) {
        return context.getSymbolValue(key);
    }

    public <T> T get(String key, Class<T> tClass) {
        return (T)context.getSymbolValue(key);
    }

    public <T> Optional<T> getOptional(String key) {
        return Optional.of((T)context.getSymbolValue(key));
    }

}
