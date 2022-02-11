package com.github.orbyfied.test;

import com.github.orbyfied.argument.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Objects;

public class ArgFunctionsTest {

    @Test
    public void test() {

        final String str = "$greeting='HELLO' --a=${print(${repeat(${greeting}, 5)})}";

        ArgContext context = new ArgContext();
        context.setFunctionValue("repeat", new StringRepeatFunction());
        context.setFunctionValue("print", new PrintFunction());

        new ArgParser().withOptions(
                new ArgOption("a", String.class, true, true)
        ).parse(str, context);

        Args args = new Args(context);

        // test

        println(args.getResult());

    }

    static void println(Object o) {
        System.out.println(o);
    }

    ////////////////////////////////

    public static class StringRepeatFunction {

        public String invoke(ArgContext context, String a, int b, Object... other) {
            return a.repeat(b);
        }

    }

    public static class PrintFunction {

        public String invoke(ArgContext context, Object o, Object... other) {
            String str = Objects.toString(o);
            System.out.println(str);
            return str;
        }

    }

}
