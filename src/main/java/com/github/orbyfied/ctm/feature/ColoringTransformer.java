package com.github.orbyfied.ctm.feature;

import com.github.orbyfied.ctm.process.Maker;
import com.github.orbyfied.ctm.process.Template;
import com.github.orbyfied.ctm.process.Transformer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColoringTransformer implements Transformer {

    private int rt;
    private int gt;
    private int bt;

    private boolean bsource;
    private boolean bborder;
    private boolean bcorners;

    public ColoringTransformer(
            boolean source, boolean border, boolean corners,
            int r, int g, int b
    ) {
        this.bsource  = source;
        this.bborder  = border;
        this.bcorners = corners;

        this.rt = r;
        this.gt = g;
        this.bt = b;
    }

    @Override
    public void transformTexFinal(Maker maker, BufferedImage result, BufferedImage source, BufferedImage border, BufferedImage corners, Template template, int borderSize, boolean inlineCorners) { }

    @Override
    public void transformPre(Maker maker, BufferedImage source, BufferedImage border, BufferedImage corners, int borderSize, boolean inlineCorners) {
        if (bsource)  recolorImage(source , rt, gt, bt);
        if (bborder)  recolorImage(border , rt, gt, bt);
        if (bcorners) recolorImage(corners, rt, gt, bt);
    }

    private static void recolorImage(BufferedImage result, int rt, int gt, int bt) {
        int w = result.getWidth();
        int h = result.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Color col = new Color(result.getRGB(x, y), true);
                int r = col.getRed();
                int g = col.getGreen();
                int b = col.getBlue();
                double l = 0.2126*r + 0.7152*g + 0.0722*b;
                result.setRGB(x, y, new Color(
                        clampcc((int)(rt * l/255)),
                        clampcc((int)(gt * l/255)),
                        clampcc((int)(bt * l/255))
                ).getRGB());
            }
        }
    }

    private static int clampcc(int in) {
        return Math.max(0, Math.min(in, 255));
    }

}
