package com.github.orbyfied.logging;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Logger {

    public static Color getLevelColor(int level) {
        Color c;
        switch (level) {
            case -5 -> c = new Color(0x292929);
            case -1 -> c = new Color(0x72B5EF);
            case  0 -> c = new Color(0x84E777);
            case  1 -> c = new Color(0xECD46B);
            case  2 -> c = new Color(0xE77878);
            default -> { throw new IllegalArgumentException(); }
        }
        return c;
    }

    public static String createLevelString(int level) {
        String s;
        switch (level) {
            case -5 -> s = "( sign)";
            case -1 -> s = "( info)";
            case  0 -> s = "(   ok)";
            case  1 -> s = "( warn)";
            case  2 -> s = "(error)";
            default -> { throw new IllegalArgumentException(); }
        }
        return s;
    }

    private String name;
    private String tag;
    private String stage;

    private List<BiConsumer<Integer, String>> outputs = new ArrayList<>();

    private boolean stackTraces = false;

    private Consumer<StringBuilder> transformer;

    public Logger(String name) {
        this.name = name;
    }

    public Logger(String name, String tag) {
        this(name);
        this.tag = tag;
    }

    public Logger stage(String s) {
        this.stage = s;
        return this;
    }

    public String stage() {
        return stage;
    }

    public Logger transformer(Consumer<StringBuilder> transformer) {
        this.transformer = transformer;
        return this;
    }

    public Logger addOutput(BiConsumer<Integer, String> out) {
        this.outputs.add(out);
        return this;
    }

    public Logger log(int level, Object... msg) {
        StringBuilder message = new StringBuilder(createLevelString(level));
        message.append(" ");
        if (tag != null || stage != null) message.append("[");
        if (tag != null) message.append(tag).append(stage != null ? "/" : "");
        if (stage != null) message.append(stage);
        if (tag != null || stage != null) message.append("]");
        message.append(" ");
        for (Object o : msg) {
            String s = Objects.toString(o);
            if (o instanceof Throwable && stackTraces) {
                Throwable t1 = (Throwable)o;
                StringWriter writer = new StringWriter();
                PrintWriter writer1 = new PrintWriter(writer);
                t1.printStackTrace(writer1);
                s = writer.toString();
            }
            message.append(s).append(" ");
        }
        message.deleteCharAt(message.length() - 1);
        if (transformer != null) transformer.accept(message);
        System.out.println(message);
        for (BiConsumer<Integer, String> out : outputs)
            out.accept(level, message.toString());
        return this;
    }

    public Logger logStage(int level, String stage, Object... msg) {
        return stage(stage).log(level, msg);
    }

    public Logger info(Object... msg) {
        return log(-1, msg);
    }

    public Logger infos(String stage, Object... msg) {
        return log(-1, stage, msg);
    }

    public Logger ok(Object... msg) {
        return log(0, msg);
    }

    public Logger oks(String stage, Object... msg) {
        return log(0, stage, msg);
    }

    public Logger warn(Object... msg) {
        return log(1, msg);
    }

    public Logger warns(String stage, Object... msg) {
        return log(1, stage, msg);
    }

    public Logger err(Object... msg) {
        return log(2, msg);
    }

    public Logger errs(String stage, Object... msg) {
        return log(2, stage, msg);
    }

}
