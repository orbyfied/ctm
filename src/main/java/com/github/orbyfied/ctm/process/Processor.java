package com.github.orbyfied.ctm.process;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Processor {

    private final Maker maker;
    public Processor(Maker maker) {
        this.maker = maker;
    }

    private Transformer transformer;

    public <T extends Transformer> T transformer(T transformer) {
        this.transformer = transformer;
        return transformer;
    }

    @SuppressWarnings("unchecked")
    public <T extends Transformer> T transformer() {
        return (T)transformer;
    }

    @SuppressWarnings("unchecked")
    public <T extends Transformer> T transformer(Class<T> tClass) {
        return (T) transformer;
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
            if (rect == null) continue;
            int x = (int)rect.getX();
            int y = (int)rect.getY();
            int w = (int)rect.getWidth();
            int h = (int)rect.getHeight();
//            /* DEBUG */ System.out.println("BORDER_RECT: " + rect + " (" + x + ", " + y + ", w: " + w+ ", h: " + h + ")");
            g.drawImage(border.getSubimage(x, y, w, h), null, x, y);
        }

        // draw optional corners
        BufferedImage from = Objects.requireNonNullElse(corners, border);
        Rectangle2D[] cornerRects = template.createCorners(inlineCorners, borderSize, source.getWidth(), source.getHeight());
        for (Rectangle2D rect : cornerRects) {
            if (rect == null) continue;
            int x = (int)rect.getX();
            int y = (int)rect.getY();
            int w = (int)rect.getWidth();
            int h = (int)rect.getHeight();
//            /* DEBUG */ System.out.println("CORNER_RECT: " + rect + " (" + x + ", " + y + ", w: " + w+ ", h: " + h + ")");
            g.drawImage(from.getSubimage(x, y, w, h), null, x, y);
        }

        // transform
        if (transformer != null)
            transformer.transformTexFinal(
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
