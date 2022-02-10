package com.github.orbyfied.ctm.process;

import com.github.orbyfied.util.StringIterator;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.StringJoiner;

public abstract class Template {

    public static SimpleTemplate parseSimple(String s) {
        boolean[] borders = new boolean[4];
        boolean[] corners = new boolean[4];

        StringIterator iter = new StringIterator(s, -1);
        char c;
        while ((c = iter.next()) != StringIterator.DONE) {
            switch (c) {
                case 'c' -> {
                    String s1 = new String(new char[] { iter.next(), iter.next() });
                    switch (s1) {
                        case "tl" -> corners[0] = true;
                        case "tr" -> corners[1] = true;
                        case "br" -> corners[2] = true;
                        case "bl" -> corners[3] = true;
                    }
                }

                case 'b' -> {
                    switch (iter.next()) {
                        case 'l' -> borders[0] = true;
                        case 't' -> borders[1] = true;
                        case 'r' -> borders[2] = true;
                        case 'b' -> borders[3] = true;
                    }
                }

                case 'a' -> {
                    switch (iter.next()) {
                        case 'b' -> Arrays.fill(borders, true);
                        case 'c' -> Arrays.fill(corners, true);
                        case 'a' -> {
                            Arrays.fill(borders, true);
                            Arrays.fill(corners, true);
                        }
                    }
                }
            }
        }

        SimpleTemplate t = new SimpleTemplate(borders, corners);
//        System.out.println(s + " -> " + t);
        return t;
    }

    public abstract Rectangle2D[] createBorders(boolean inlineCorners, int borderSize, int w, int h);
    public abstract Rectangle2D[] createCorners(boolean inlineCorners, int borderSize, int w, int h);

    public static final int ALL_TILES_COUNT  = 47;
    public static final Template[] ALL_TILES;

    static {
        ALL_TILES = new Template[] {
                parseSimple("ab"), // 0

                parseSimple("bb bl bt"),  // 1
                parseSimple("bb bt"),     // 2
                parseSimple("bb br bt"),  // 3

                parseSimple("bl bt cbr"), // 4
                parseSimple("br bt cbl"), // 5

                parseSimple("bl ctr cbr"), // 6
                parseSimple("bt cbl cbr"), // 7

                parseSimple("ctl cbl cbr"), // 8
                parseSimple("ctr ctl cbl"), // 9
                parseSimple("ctr cbr"),     // 10
                parseSimple("cbl cbr"),     // 11

                parseSimple("bl bt br"), // 12
                parseSimple("bl bt"),    // 13
                parseSimple("bt"),       // 14
                parseSimple("bt br"),    // 15

                parseSimple("bl bb ctr"), // 16
                parseSimple("br bb ctl"), // 17

                parseSimple("bb ctl ctr"), // 18
                parseSimple("br ctl cbl"), // 19

                parseSimple("cbl cbr ctr"), // 20
                parseSimple("cbl cbr ctr"), // 21
                parseSimple("ctl ctr"),     // 22
                parseSimple("ctl cbl"),     // 23

                parseSimple("bl br"), // 24

                parseSimple("bl"), // 25
                parseSimple(""),   // 26
                parseSimple("br"), // 27

                parseSimple("bl ctr"), // 28
                parseSimple("bt cbr"), // 29
                parseSimple("bl cbr"), // 30
                parseSimple("cbl bt"), // 31

                parseSimple("cbr"), // 32
                parseSimple("cbl"), // 33

                parseSimple("ctl cbr"), // 34
                parseSimple("cbl ctr"), // 35

                parseSimple("bl bb br"), // 36

                parseSimple("bl bb"), // 37
                parseSimple("bb"),    // 38
                parseSimple("bb br"), // 39

                parseSimple("ctl bb"), // 30
                parseSimple("cbl br"), // 41

                parseSimple("bb ctr"), // 42
                parseSimple("br ctl"), // 43

                parseSimple("ctr"), // 44
                parseSimple("ctl"), // 45
                parseSimple("ac")   // 46
        };
    }

    public static class SimpleTemplate extends Template {

        public SimpleTemplate(boolean[] borders, boolean[] corners) {
            this.borders = borders;
            this.corners = corners;
        }

        final boolean[] borders;
        final boolean[] corners;

        @Override
        public Rectangle2D[] createBorders(
                boolean inlineCorners,
                int bs,
                int w,
                int h) {
            Rectangle2D[] rects = new Rectangle2D[4];
            if (borders[0]) rects[0] = new Rectangle(0, 0, bs, h);
            if (borders[1]) rects[1] = new Rectangle(0, 0, w, bs);
            if (borders[2]) rects[2] = new Rectangle(w - bs, 0, bs, h);
            if (borders[3]) rects[3] = new Rectangle(0, h - bs, w, bs);
            return rects;
        }

        @Override
        public Rectangle2D[] createCorners(
                boolean inlineCorners,
                int bs,
                int w,
                int h) {
            Rectangle2D[] rects = new Rectangle2D[4];
            if (corners[0]) rects[0] = new Rectangle(0, 0, bs, bs);
            if (corners[1]) rects[1] = new Rectangle(w - bs, 0, bs, bs);
            if (corners[2]) rects[2] = new Rectangle(w - bs, h - bs, bs, bs);
            if (corners[3]) rects[3] = new Rectangle(0, h - bs, bs, bs);
            return rects;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SimpleTemplate.class.getSimpleName() + "[", "]")
                    .add("borders=" + Arrays.toString(borders))
                    .add("corners=" + Arrays.toString(corners))
                    .toString();
        }
    }

}
