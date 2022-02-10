package com.github.orbyfied.ctm.process;

import java.awt.image.BufferedImage;

public interface Transformer {

    void transformTexFinal(
            Maker maker,

            BufferedImage result,
            BufferedImage source,
            BufferedImage border,
            BufferedImage corners,
            Template template,
            int borderSize,
            boolean inlineCorners
    );

    void transformPre(
            Maker maker,

            BufferedImage source,
            BufferedImage border,
            BufferedImage corners,
            int borderSize,
            boolean inlineCorners
    );

}
