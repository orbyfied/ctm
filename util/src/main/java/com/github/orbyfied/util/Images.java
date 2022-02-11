package com.github.orbyfied.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Images {

    /**
     * From https://www.rgagnon.com/javadetails/java-0601.html
     */
    public static BufferedImage imageToBufferedImage(Image im) {
        BufferedImage bi = new BufferedImage
                (im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

}
