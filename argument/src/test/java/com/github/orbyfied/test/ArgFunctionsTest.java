package com.github.orbyfied.test;

import com.github.orbyfied.argument.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Objects;

public class ArgFunctionsTest {

    @Test
    public void test() {

        // TODO: fix function parsing
        // it for some reason doesnt work after a call or something

        final String str =
                "$greeting='HELLO' " +
                "$greeting2='HEY' " +
                "--a=${_print(${_repeat(${greeting}, 5)})} " +
                "--b=${_print(${_repeat('5', 2)})} " +
                "--c=${_sqr(8)} " +
                "--d=${_print(${_repeat(${greeting2}, 5)})} " +
                "";

        ArgContext context = new ArgContext();
        StdContext.apply(context);

        new ArgParser().withOptions(
                new ArgOption("a", String.class, true, false),
                new ArgOption("b", Double.class, true, false),
                new ArgOption("c", Double.class, true, false),
                new ArgOption("d", String.class, true, false)
        ).parse(str, context);

        Args args = new Args(context);

        // test

        println(args.getResult());

    }

    static void println(Object o) {
        System.out.println(o);
    }

    ////////////////////////////////

}
