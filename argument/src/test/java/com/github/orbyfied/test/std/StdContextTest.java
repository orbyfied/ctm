package com.github.orbyfied.test.std;

import com.github.orbyfied.argument.Args;
import org.junit.jupiter.api.Test;

public class StdContextTest {

    @Test
    public void test() {

        final String str = "$myPixel=${_u_getbipx2416(${_u_loadbimage('C:\\\\Users\\\\atomf\\\\Documents\\\\red.png')}, 5, 5)}";

        Args args = new Args();
        args.parse(str, null);

        // test

        println(args.getResult());

        println(args.get("$myPixel"));

    }

    static void println(Object o) {
        System.out.println(o);
    }

}
