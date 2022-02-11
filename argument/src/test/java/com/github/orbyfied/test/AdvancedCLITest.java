package com.github.orbyfied.test;

import com.github.orbyfied.argument.ArgOption;
import com.github.orbyfied.argument.Args;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AdvancedCLITest {

    @Test
    public void test() {

        final String str = "$hello=\"hi\" --hey=${hello}";

        Args args = new Args();
        args.parse(str, parser -> parser.withOptions(
                new ArgOption("hey", String.class, true, true)
        ));

        assertEquals(args.get("hey"), "hi");

    }

}
