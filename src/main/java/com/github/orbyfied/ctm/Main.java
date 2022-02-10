package com.github.orbyfied.ctm;

import com.github.orbyfied.argument.ArgOption;
import com.github.orbyfied.argument.Args;
import com.github.orbyfied.ctm.process.Maker;

import java.nio.file.Path;

public class Main {

    public static Maker maker;

    public static void main(String[] args1) {

        // construct arguments
        StringBuilder argsf = new StringBuilder();
        for (String arg : args1)
            argsf.append(arg).append(" ");

        // String str = argsf.toString();
        /* DEBUG */ String str = "source.png border.png 15 --archive=\"wool\" --output-dir=out --corner-image=corners.png -it";

        // parse them
        Args args = new Args();
        args.parse(str, parser -> parser.withOptions(
                new ArgOption("source-image", Path.class,  false, true),
                new ArgOption("border-image", Path.class,  false, true),
                new ArgOption("border-size",  Integer.class, false, true),

                new ArgOption("archive-name", String.class, true, true, "archive"),
                new ArgOption("output-dir", Path.class, true, false),

                new ArgOption("corner-image",   Path.class,  true, false),
                new ArgOption("inline-corners", Boolean.class, true, false).withShortAliases('i'),
                new ArgOption("test-border", Boolean.class, true, false).withShortAliases('t')
        ));

        // construct maker and export
        maker = new Maker("CTM");

        maker.outputDir = args.get("output-dir");
        maker.archiveName = args.get("archive-name");

        maker.sourceImage = args.get("source-image");
        maker.borderImage = args.get("border-image");
        maker.borderSizePx = args.get("border-size");

        maker.cornerImage      = args.get("corner-image");
        maker.useInlineCorners = args.get("inline-corners");

        // export
        maker.loadImages();
        if (maker.testBorderSize)
            maker.testBorderSize();
        maker.export();

    }

}
