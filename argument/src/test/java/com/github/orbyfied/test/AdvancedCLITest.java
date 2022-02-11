package com.github.orbyfied.test;

import com.github.orbyfied.argument.ArgOption;
import com.github.orbyfied.argument.Args;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class AdvancedCLITest {

    @Test
    public void test() {

        // test string
        final String str = "$hello=\"hi\" --a=${hello} --b='hello:${hello}'";

        // parse
        Args args = new Args();
        args.parse(str, parser -> parser.withOptions(
                new ArgOption("a", Path.class, true, true),
                new ArgOption("b", String.class, true, true)
//                new ArgOption("c", Integer.class, true, true)
        ));

        // test

        println(args.getResult());

        assertEquals(Path.of("hi"), args.get("a"));
        assertEquals("hello:" + args.get("$hello"), args.get("b"));

    }

    static void println(Object o) {
        System.out.println(o);
    }

}
