package com.github.orbyfied.ctm.process;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Processor {

    private final Maker maker;
    public Processor(Maker maker) {
        this.maker = maker;
    }

    private Transformer transformer;

    public Processor transformer(Transformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public Transformer transformer() {
        return transformer;
    }

    public void prepare() {
        if (transformer != null)
            transformer.transformPre(
                    maker,
                    maker.sourceImage,
                    maker.borderImage,
                    maker.cornerImage,
                    maker.borderSizePx,
                    maker.useInlineCorners);
    }

    public void exportTexture(
            BufferedImage result,
            BufferedImage source,
            BufferedImage border,
            BufferedImage corners,
            Template template,
            int borderSize,
            boolean inlineCorners
    ) {
        // get/create result graphics
        Graphics2D g = result.createGraphics();

        // draw source image
        g.drawImage(source, null, 0, 0);

        // draw border image
        Rectangle2D[] borderRects = template.createBorders(inlineCorners, borderSize, source.getWidth(), source.getHeight());
        for (Rectangle2D rect : borderRects) {
            int x = (int)rect.getX();
            int y = (int)rect.getY();
            int w = (int)rect.getWidth();
            int h = (int)rect.getHeight();
            g.drawImage(border.getSubimage(x, y, w, h), null, x, y);
        }

        // draw optional corners
        if (corners != null || inlineCorners) {
            Rectangle2D[] cornerRects = template.createCorners(inlineCorners, borderSize, source.getWidth(), source.getHeight());
            for (Rectangle2D rect : cornerRects) {
                int x = (int)rect.getX();
                int y = (int)rect.getY();
                int w = (int)rect.getWidth();
                int h = (int)rect.getHeight();

                if (corners != null) g.drawImage(corners.getSubimage(x, y, w, h), null, x, y);
                else g.drawImage(source.getSubimage(x, y, w, h), null, x, y);
            }
        }

        // transform
        if (transformer != null)
            transformer.transformFinal(
                    maker,
                    result,
                    source,
                    border,
                    corners,
                    template,
                    borderSize,
                    inlineCorners);
    }

}
