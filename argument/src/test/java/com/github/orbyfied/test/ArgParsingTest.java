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

        String str =
                " --test-folder='C:\\Test 123'" + // test-folder
                " --strings+=a --strings+=b --strings+=c" + // strings
                " --other-strings='[\"a b c\", def, \"x y z\"]'" + // other-strings
                " -ex" + // enable-something, enable-something-else
                " \"Hello World!\"" + // foo
                " " // bar (not set to test defaulting)
                ;

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

        // TESTING

        println(args.getResult());

        assertEquals(args.get("test-folder"), Path.of("C:\\Test 123"));
        assertArrayEquals(args.get("strings", List.class).toArray(new String[0]), new String[] { "a", "b", "c" });
        assertArrayEquals(args.get("other-strings", List.class).toArray(new String[0]), new String[] { "a b c", "def", "x y z" });
        assertEquals(args.get("enable-something"), true);
        assertEquals(args.get("enable-something-else"), true);
        assertEquals(args.get("foo"), "Hello World!");
        assertEquals(args.get("bar"), (Integer)69);

    }

    static void println(Object o) {
        System.out.println(o);
    }

}
