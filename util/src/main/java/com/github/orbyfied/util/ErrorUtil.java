package com.github.orbyfied.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorUtil {

    public static String getStackTrace(Throwable t1) {
        if (t1 == null) return "<no err>";
        StringWriter writer = new StringWriter();
        PrintWriter writer1 = new PrintWriter(writer);
        t1.printStackTrace(writer1);
        return writer.toString();
    }

}
