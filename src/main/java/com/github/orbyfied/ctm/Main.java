package com.github.orbyfied.ctm;

import com.github.orbyfied.argument.ArgOption;
import com.github.orbyfied.argument.ArgType;
import com.github.orbyfied.argument.Args;
import com.github.orbyfied.ctm.feature.OverlayMirroringTransformer;
import com.github.orbyfied.ctm.process.ChainedTransformer;
import com.github.orbyfied.ctm.process.Maker;
import com.github.orbyfied.ctm.process.Match;
import com.github.orbyfied.util.StringIterator;

import java.nio.file.Path;
import java.text.StringCharacterIterator;

public class Main {

    public static Maker maker;

    public static void main(String[] args1) {

        // construct arguments
        StringBuilder argsf = new StringBuilder();
        for (String arg : args1)
            argsf.append(arg).append(" ");

         String str = argsf.toString();
//        /* DEBUG */ String str = "source.png border.png 15 --archive=\"wool\" --output-dir=out --corner-image=corners.png -it";

        // parse them
        Args args = new Args();
        args.parse(str, parser -> parser.withOptions(
                new ArgOption("source-image", Path.class,  false, true),
                new ArgOption("border-image", Path.class,  false, true),
                new ArgOption("border-size",  Integer.class, false, true),

                new ArgOption("archive-name", String.class, true, true, "archive"),
                new ArgOption("output-dir", Path.class, true, false),
                new ArgOption("matches", ArgType.listing(Match.class, Main::parseMatch), true, true),

                new ArgOption("corner-image",   Path.class,  true, false),
                new ArgOption("inline-corners", Boolean.class, true, false).withShortAliases('i'),
                new ArgOption("test-border", Boolean.class, true, false).withShortAliases('t'),
                new ArgOption("mirror-overlays", ArgType.mono(OverlayMirroringTransformer.class, Main::parseOverlayMirroring), true, false)
        ));

        // construct maker and export
        maker = new Maker("CTM");

        maker.outputDir = args.get("output-dir");
        maker.archiveName = args.get("archive-name");
        maker.matches = args.get("matches");

        maker.sourceImagePath = args.get("source-image");
        maker.borderImagePath = args.get("border-image");
        maker.borderSizePx = args.get("border-size");

        maker.cornerImagePath  = args.get("corner-image");
        maker.useInlineCorners = args.get("inline-corners");
        maker.testBorderSize   = args.get("test-border");

        // add transformers
        ChainedTransformer transformer = maker.getProcessor().transformer(new ChainedTransformer());
        transformer.addTransformer(args.get("mirror-overlays"));

        // export
        maker.loadImages();
        if (maker.testBorderSize)
            maker.testBorderSize();
        maker.export();

    }

    //////////////////////////////////////////////////

    /** Match parser for the arguments. */
    private static Object parseMatch(String s) {
        String[] split = s.split(":");
        if (split.length < 2)
            throw new IllegalArgumentException("missing match parameters ('<tilename>:<matches>')");
        String tileName = split[0];
        String matches  = split[1];
        return new Match(null).withProperties(matches, tileName);
    }

    /** Mirroring parser for the arguments. */
    private static OverlayMirroringTransformer parseOverlayMirroring(String s) {
        OverlayMirroringTransformer t = new OverlayMirroringTransformer();
        StringIterator iter = new StringIterator(s, -1);
        char c;
        while ((c = iter.next()) != StringIterator.DONE) {
            switch (c) {
                case 'h' -> t.setMirrorH(true);
                case 'v' -> t.setMirrorV(true);

                case 'b' -> t.setDoBorders(true);
                case 'c' -> t.setDoCorners(true);
            }
        }
        return t;
    }

}
