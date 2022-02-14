package com.github.orbyfied.ctm;

import com.github.orbyfied.argument.ArgOption;
import com.github.orbyfied.argument.ArgType;
import com.github.orbyfied.argument.Args;
import com.github.orbyfied.ctm.feature.ColoringTransformer;
import com.github.orbyfied.ctm.feature.OverlayMirroringTransformer;
import com.github.orbyfied.ctm.gui.CtmGui;
import com.github.orbyfied.ctm.process.ChainedTransformer;
import com.github.orbyfied.ctm.process.Maker;
import com.github.orbyfied.ctm.process.Match;
import com.github.orbyfied.ctm.process.Template;
import com.github.orbyfied.logging.Logger;
import com.github.orbyfied.util.StringIterator;
import com.github.orbyfied.util.Vec2;

import java.nio.file.Path;
import java.text.StringCharacterIterator;

public class Main {

    public static final String VERSION = "0.2.2R5";

    public static Maker maker;
    public static CtmGui gui;

    public static void main(String[] args1) {

        long t1;

        // construct arguments
        StringBuilder argsf = new StringBuilder();
        for (String arg : args1)
            argsf.append(arg).append(" ");

        String str = argsf.toString();

        // construct maker
        maker = new Maker("CTM");
        maker.reset();
        Logger logger = maker.logger;
        logger.log(-5, "-> ctm " + VERSION + " by orbyfied (https://github.com/orbyfied/ctm)");

        if (args1.length == 0) {
            printHelpMessage();
            enterGui();
            return;
        }

        // parse arguments
        t1 = System.nanoTime();

        logger.stage("parse-args");
        logger.info("parsing command line");
        Args args = new Args();
        args.parse(str, parser -> parser.withOptions(
                new ArgOption("source-image", Path.class,  false, true),
                new ArgOption("border-image", Path.class,  false, true),
                new ArgOption("border-size",  Integer.class, false, true),

                new ArgOption("archive-name", String.class, true, true, "archive"),
                new ArgOption("output-dir", Path.class, true, false),
                new ArgOption("matches", ArgType.listing(Match.class, Main::parseMatch), true, true),

                new ArgOption("corner-overlay",   Path.class,  true, false),
                new ArgOption("inline-corners", Boolean.class, true, false).withShortAliases('i'),
                new ArgOption("test-border", Boolean.class, true, false).withShortAliases('t'),
                new ArgOption("block-texture", Boolean.class, true, false).withDefault(true),

                new ArgOption("mirror-overlays", ArgType.mono(OverlayMirroringTransformer.class, Main::parseOverlayMirroring), true, false),
                new ArgOption("recolor", ArgType.mono(ColoringTransformer.class, Main::parseColoringTransformer), true, false),

                new ArgOption("rescale", ArgType.mono(Vec2.class, Main::parseVec2), true, false)
        ).withWarningHandler(logger::warn));

        // initialize properties
        maker.outputDir = args.get("output-dir");
        maker.archiveName = args.get("archive-name");
        maker.matches = args.get("matches");

        maker.sourceImagePath = args.get("source-image");
        maker.borderImagePath = args.get("border-image");
        maker.borderSizePx = args.get("border-size");

        maker.cornerImagePath  = args.get("corner-overlay");
        maker.useInlineCorners = args.get("inline-corners");
        maker.testBorderSize   = args.get("test-border");
        maker.makeBlockFile    = args.get("block-texture");

        maker.rescale = args.get("rescale");

        // add transformers
        ChainedTransformer transformer = maker.getProcessor().transformer(new ChainedTransformer());
        transformer.addTransformer(args.get("mirror-overlays"));
        transformer.addTransformer(args.get("recolor"));

        // log done parsing
        logger.ok("parsed command line in " + getTimeElapsed(t1));

        // export
        doExport();

    }

    public static void doExport() {
        Logger logger = maker.logger;

        // export
        long t1 = System.nanoTime();
        logger.stage("export").info("preparing for export");
        maker.prepareExport();
        try {
            maker.loadImages();
            if (maker.testBorderSize)
                maker.testBorderSize();
            logger.info("exporting " + Template.ALL_TILES_COUNT + " tile(s) and " + maker.matches.size() + " match(es)");
            boolean b = maker.export();
            if (b) logger.stage("export").ok("successfully exported in " + getTimeElapsed(t1));
            else logger.stage("export").err("failed to export; error logged above (elapsed: " + getTimeElapsed(t1) + ")");
        } catch (Exception e) {
            logger.err("error while exporting (elapsed: " + getTimeElapsed(t1) + "):", e);
            e.printStackTrace();
        }
    }

    public static void enterGui() {
        maker.logger.stage("gui").info("entering GUI mode");
        gui = new CtmGui();
        gui.open();
    }

    private static String getTimeElapsed(long nanos) {
        long res = System.nanoTime() - nanos;
        return res + "ns (" + res / 1_000_000 + "ms)";
    }

    //////////////////////////////////////////////////

    /** For parsing matches from the command line. */
    private static Object parseMatch(String s) {
        String[] split = s.split(":");
        if (split.length < 2)
            throw new IllegalArgumentException("missing match parameters ('<tilename>:<matches>') for input: " + s);
        String tileName = split[0];
        String matches  = split[1];
        return new Match(null).withProperties(matches, tileName);
    }

    /** For parsing mirroring transformer from the command line. */
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

    /** For parsing coloring transformer from arguments. */
    private static ColoringTransformer parseColoringTransformer(String s) {
        String[] split = s.split(":");
        if (split.length < 2 || split[1].length() < 6)
            throw new IllegalArgumentException("invalid recolor code: " + s);
        boolean docorners = false;
        boolean dosource = false;
        boolean doborder = false;
        StringIterator iter = new StringIterator(s, -1);
        char c;
        while ((c = iter.next()) != StringIterator.DONE) {
            switch (c) {
                case 's' -> dosource = true;
                case 'b' -> doborder = true;
                case 'c' -> docorners = true;
            }
        }
        String hex = split[1];
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        if (split.length == 3) {
            String[] split1 = split[2].split(",");
            if (split1.length != 3)
                throw new IllegalArgumentException("invalid channel multiplier setting (expected 'r,g,b'): " + split[2]);
            r *= Float.parseFloat(split1[0]);
            g *= Float.parseFloat(split1[1]);
            b *= Float.parseFloat(split1[2]);
        }
        return new ColoringTransformer(dosource, doborder, docorners, r, g, b);
    }

    /** For parsing Vec2s from the command line. */
    private static Vec2 parseVec2(String s) {
        s = s.substring(s.startsWith("(") ? 1 : 0, s.endsWith(")") ? s.length() - 1 : s.length());
        String[] split = s.split(",");
        if (split.length < 2)
            throw new IllegalArgumentException("invalid vec2i (expected '(<x>,<y>)'): " + s);
        return new Vec2(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    /**
     * Prints the help message.
     */
    private static void printHelpMessage() {
        println();
        println(" > Command Line Usage: ");
        println(" ' ctm <source> " +
                "<border> " +
                "<bordersize> " +
                "<--archive-name=...> " +
                "[--output-dir=...] " +
                "<--matches+=<name>:<match>> " +
                "[--corner-overlay=...] " +
                "[--test-border] " +
                "[--rescale=(w,h)] '");
        println();
    }

    private static void println(Object o) {
        System.out.println(o);
    }

    private static void println() {
        System.out.println();
    }

}
