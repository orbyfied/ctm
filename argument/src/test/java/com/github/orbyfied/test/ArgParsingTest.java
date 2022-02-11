package com.github.orbyfied.test;

import static org.junit.jupiter.api.Assertions.*;

import com.github.orbyfied.argument.ArgOption;
import com.github.orbyfied.argument.ArgType;
import com.github.orbyfied.argument.Args;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

public class ArgParsingTest {

    @Test
    @SuppressWarnings("unchecked")
    public void test() {

        // test string
        final String str =
                " --test-folder='C:\\\\Test 123'" + // test-folder
                " --strings+=a --strings+=b --strings+=c" + // strings
                " --other-strings='[\"a b c\", def, \"x y z\"]'" + // other-strings
                " -ex" + // enable-something, enable-something-else
                " \"Hello World!\"" + // foo
                " " // bar (not set to test defaulting)
                ;

        // parse
        Args args = new Args();
        args.parse(str, parser -> parser.withOptions(

            new ArgOption("test-folder", Path.class, true, true),
            new ArgOption("strings", ArgType.listing(String.class), true, true),
            new ArgOption("other-strings", ArgType.listing(String.class), true, true),
            new ArgOption("enable-something", Boolean.class, true, true)
                    .withShortAliases('e'),
            new ArgOption("enable-something-else", Boolean.class, true, false)
                    .withShortAliases('x'),
            new ArgOption("foo", String.class, false, true),
            new ArgOption("bar", Integer.class, true, false)
                .withDefault(69)

        ));

        // testing

        println(args.getResult());

        assertEquals(Path.of("C:\\Test 123"), args.get("test-folder"));
        assertArrayEquals(new String[] { "a", "b", "c" }, args.get("strings", List.class).toArray(new String[0]));
        assertArrayEquals(new String[] { "a b c", "def", "x y z" }, args.get("other-strings", List.class).toArray(new String[0]));
        assertEquals(true, args.get("enable-something"));
        assertEquals(true, args.get("enable-something-else"));
        assertEquals("Hello World!", args.get("foo"));
        assertEquals((Integer)69, args.get("bar"));

    }

    static void println(Object o) {
        System.out.println(o);
    }

}
