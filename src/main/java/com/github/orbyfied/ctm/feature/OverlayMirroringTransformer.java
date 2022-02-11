package com.github.orbyfied.ctm.feature;

import com.github.orbyfied.ctm.process.Maker;
import com.github.orbyfied.ctm.process.Template;
import com.github.orbyfied.ctm.process.Transformer;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class OverlayMirroringTransformer implements Transformer {

    public OverlayMirroringTransformer() { }

    public OverlayMirroringTransformer(boolean h, boolean v, boolean db, boolean dc) {
        this.mirrorH = h;
        this.mirrorV = v;
        this.doCorners = dc;
        this.doBorders = db;
    }

    private boolean mirrorH;
    private boolean mirrorV;

    private boolean doBorders;
    private boolean doCorners;

    public void setDoBorders(boolean b) {
        this.doBorders = b;
    }

    public void setDoCorners(boolean b) {
        this.doCorners = b;
    }

    public boolean isDoBorders() {
        return doBorders;
    }

    public boolean isDoCorners() {
        return doCorners;
    }

    public void setMirrorH(boolean b) {
        this.mirrorH = b;
    }

    public void setMirrorV(boolean b) {
        this.mirrorV = b;
    }

    public boolean isMirrorH() {
        return mirrorH;
    }

    public boolean isMirrorV() {
        return mirrorV;
    }

    @Override
    public void transformTexFinal(Maker maker, BufferedImage result, BufferedImage source, BufferedImage border, BufferedImage corners, Template template, int borderSize, boolean inlineCorners) { }

    @Override
    public void transformPre(Maker maker, BufferedImage source, BufferedImage border, BufferedImage corners, int borderSize, boolean inlineCorners) {

        if (doBorders) maker.borderImage = mirrorImage(border, mirrorH, mirrorV);
        if (corners != null && doCorners) maker.cornerImage = mirrorImage(corners, mirrorH, mirrorV);

    }

    private static BufferedImage mirrorImage(BufferedImage image, boolean mh, boolean mv) {

        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        if (mv) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w / 2; x++) {
                    int rx = w - x - 1;
                    int c  = image.getRGB(x, y);
                    copy.setRGB(x,  y, c);
                    copy.setRGB(rx, y, c);
                }
            }
        }
        if (mh) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < w / 2; y++) {
                    int ry = h - y - 1;
                    int c  = copy.getRGB(x, y);
                    copy.setRGB(x,  y, c);
                    copy.setRGB(x, ry, c);
                }
            }
        }

        return copy;

    }

}
