package com.github.orbyfied.ctm.process;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ChainedTransformer implements Transformer {

    final List<Transformer> transformers = new ArrayList<>();

    public ChainedTransformer addTransformer(Transformer transformer) {
        if (transformer == null) return this;
        transformers.add(transformer);
        return this;
    }

    public ChainedTransformer removeTransformer(Transformer transformer) {
        transformers.remove(transformer);
        return this;
    }

    @Override
    public void transformTexFinal(Maker maker, BufferedImage result, BufferedImage source, BufferedImage border, BufferedImage corners, Template template, int borderSize, boolean inlineCorners) {
        for (Transformer t : transformers)
            t.transformTexFinal(maker, result, source, border, corners, template, borderSize, inlineCorners);
    }

    @Override
    public void transformPre(Maker maker, BufferedImage source, BufferedImage border, BufferedImage corners, int borderSize, boolean inlineCorners) {
        for (Transformer t : transformers)
            t.transformPre(maker, source, border, corners, borderSize, inlineCorners);
    }

}
