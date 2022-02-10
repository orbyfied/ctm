package com.github.orbyfied.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class IOUtil {

    public static PrintWriter createFilePrintWriter(Path path) {
        try {
            return new PrintWriter(Files.newOutputStream(path));
        } catch (IOException e) { throw new IllegalStateException(e); }
    }

}
