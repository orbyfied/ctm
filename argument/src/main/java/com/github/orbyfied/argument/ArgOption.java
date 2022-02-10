package com.github.orbyfied.argument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class ArgOption {

    private ArgType<?, ?> type;
    private String name;
    private List<String> aliases;
    private List<Character> shortAliases;
    private boolean named;
    private boolean required;
    private Object defaultValue;

    public ArgOption(
            String name,
            ArgType<?, ?> type,
            boolean named, boolean required,
            String... aliases) {
        this.name = name;
        this.type = type;
        this.aliases = new ArrayList<>(Arrays.asList(aliases));
        this.named = named;
        this.required = required;
    }

    public ArgOption(
            String name,
            Class<?> type,
            boolean named, boolean required,
            String... aliases
    ) {
        this(name, ArgType.mono(type), named, required, aliases);
    }

    public ArgOption withDefault(Object o) {
        this.defaultValue = o;
        return this;
    }

    public ArgOption withShortAliases(Character... shortAliases) {
        this.shortAliases = Arrays.asList(shortAliases);
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean hasShort() {
        return shortAliases != null;
    }

    public String getName() {
        return name;
    }

    public ArgType<?, ?> getType() {
        return type;
    }

    public boolean isNamed() {
        return named;
    }

    public boolean isRequired() {
        return required;
    }

    public List<String> aliases() {
        return aliases;
    }

    public List<Character> shorts() {
        return shortAliases;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ArgOption.class.getSimpleName() + "(", ")")
                .add("type=" + type)
                .add("name='" + name + "'")
                .add("aliases=" + aliases)
                .add("shortAliases=" + shortAliases)
                .add("named=" + named)
                .add("required=" + required)
                .add("defaultValue=" + defaultValue)
                .toString();
    }
}
