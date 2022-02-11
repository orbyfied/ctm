package com.github.orbyfied.ctm.process;

import com.github.orbyfied.util.IOUtil;
import com.github.orbyfied.logging.Logger;
import com.github.orbyfied.util.Images;
import com.github.orbyfied.util.Vec2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Maker {

    public Maker(String name) {
        this.name = name;
    }

    /* Properties. */

    public String name;

    public String archiveName;
    public List<Match> matches;
    public Path outputDir;
    public Path archiveDir;

    public Path sourceImagePath;
    public Path borderImagePath;
    public Path cornerImagePath;
    public int borderSizePx;
    public boolean testBorderSize;
    public Vec2 rescale;

    public boolean useInlineCorners;

    /* Loaded. */

    public BufferedImage sourceImage;
    public BufferedImage borderImage;
    public BufferedImage cornerImage;

    /* Global Logger. */
    public final Logger logger = new Logger("CTM", "CTM");

    /**
     * The processor for this maker.
     */
    private final Processor processor = new Processor(this);

    //////////////////////////////////////////

    public Maker addMatch(Match match) {
        matches.add(match);
        return this;
    }

    public Maker withProperty(String name, Object o) {
        try {
            Maker.class.getField(name).set(this, o);
            return this;
        } catch (NoSuchFieldException e) { return this; } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    public Processor getProcessor() {
        return processor;
    }

    //////////////////////////////////////////

    public void prepareExport() {
        logger.stage("export");
    }

    /**
     * Loads all necessary images.
     */
    public void loadImages() {

        logger.info("loading images").stage("image-load");

        if (
                sourceImagePath == null ||
                !Files.exists(sourceImagePath)
        ) {
            logger.err("source image was not provided or does not exist;", sourceImagePath);
            return;
        }

        if (
                borderImagePath == null ||
                !Files.exists(borderImagePath)
        ) {
            logger.err("border overlay image was not provided or does not exist;", borderImagePath);
            return;
        }

        try {

            // load source and border images
            logger.info("loading source image from " + sourceImagePath);
            sourceImage = ImageIO.read(sourceImagePath.toFile());
            logger.info("loading border image from " + borderImagePath);
            borderImage = ImageIO.read(borderImagePath.toFile());

            // load optional corner image
            if (cornerImagePath != null) {
                if (Files.exists(cornerImagePath)) {
                    logger.info("loading corner overlay image from " + cornerImagePath);
                    cornerImage = ImageIO.read(cornerImagePath.toFile());
                } else logger.warn("corner overlay image does not exist. skipping");
            }

            // rescale to fit size
            Vec2 scale = this.rescale;
            if (scale == null)
                scale = new Vec2(sourceImage.getWidth(), sourceImage.getHeight());
            if (scale.x() != scale.y()) {
                logger.err("invalid scale: not square (" + scale.x() + "x" + scale.y() + ")");
                return;
            }

            if ( // source image if needed
                    sourceImage.getWidth()  != scale.x() ||
                    sourceImage.getHeight() != scale.y()) {
                int w = sourceImage.getWidth();
                int h = sourceImage.getHeight();
                logger.info("rescaling source image from " + w + "x" + h + " to " + scale.x() + "x" + scale.y());
                sourceImage = Images.imageToBufferedImage(sourceImage.getScaledInstance(scale.x(), scale.y(), Image.SCALE_SMOOTH));
            }

            if ( // border image if needed
                    borderImage.getWidth()  != scale.x() ||
                    borderImage.getHeight() != scale.y()) {
                int w = borderImage.getWidth();
                int h = borderImage.getHeight();
                if (rescale == null)
                    logger.warn("border image size (" + w + "x" + h + ") " +
                            "differs from source image size (" + scale.x() + "x" + scale.y() + ")");
                logger.info("rescaling border image from " + w + "x" + h + " to " + scale.x() + "x" + scale.y());
                borderImage = Images.imageToBufferedImage(borderImage.getScaledInstance(scale.x(), scale.y(), Image.SCALE_SMOOTH));
            }

            if ( // corner overlay if it is used and if needed
                    cornerImage != null &&
                    (cornerImage.getWidth()  != scale.x() ||
                    cornerImage.getHeight() != scale.y())) {
                int w = cornerImage.getWidth();
                int h = cornerImage.getHeight();
                if (rescale == null)
                    logger.warn("corner image size (" + w + "x" + h + ") " +
                            "differs from source image size (" + scale.x() + "x" + scale.y() + ")");
                logger.info("rescaling corner image from " + w + "x" + h + " to " + scale.x() + "x" + scale.y());
                cornerImage = Images.imageToBufferedImage(cornerImage.getScaledInstance(scale.x(), scale.y(), Image.SCALE_SMOOTH));
            }

            // ok
            logger.ok("successfully loaded images");

        } catch (IOException e) {
            logger.err("io error while loading images:", e);
        }

    }

    public boolean export() {
        logger.stage("export");

        // check border size
        if (borderSizePx <= 0) {
            logger.err("border size is negative or zero: " + borderSizePx);
            return false;
        }

        if (borderSizePx >= sourceImage.getWidth()  / 2 ||
                borderSizePx >= sourceImage.getHeight() / 2) {
            logger.err("border size is larger than size/2 (" + sourceImage.getWidth()/2 + "), could cause math errors: " + borderSizePx);
            return false;
        }

        // prepare
        processor.prepare();

        // export all images
        logger.stage("export-tiles");
        for (int i = 0; i < Template.ALL_TILES_COUNT; i++) {

            try {
                // get files
                Path outputFile = getOutputFile(i + ".png");

                // delete if it exists already
                if (Files.exists(outputFile))
                    Files.delete(outputFile);
                Files.createFile(outputFile);

                // log info
                logger.info("processing tile " + i + " | out: " + outputFile);

                // get tile template
                Template template = Template.ALL_TILES[i];

                // create result image
                BufferedImage result = new BufferedImage(
                        sourceImage.getWidth(), sourceImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );

                // process
                processor.exportTexture(
                        result,
                        sourceImage,
                        borderImage,
                        cornerImage,
                        template,
                        borderSizePx,
                        useInlineCorners
                );

                // write to file
                ImageIO.write(result, "PNG", outputFile.toFile());

                // log ok
//                logger.ok("processed tile " + i + " | " + outputFile);
            } catch (Exception e) {
                logger.err("error while processing tile " + i + ":", e);
            }
        }

        // write meta files
        logger.stage("export-meta");
        logger.info("writing " + matches.size() + " meta files");
        try {
            for (Match match : matches) {
                Path p = match.getFile(archiveDir);
                logger.info("writing " + p + " | " + match);
                if (Files.exists(p))
                    Files.delete(p);
                Files.createFile(p);
                PrintWriter writer = IOUtil.createFilePrintWriter(p);
                match.writeFile(p, writer);
                writer.close();
            }
        } catch (Exception e) {
            logger.err("exception while writing meta files:", e);
            return false;
        }

        return true;
    }

    public int testBorderSize() {
        logger.stage("test-border-size").info("testing border size");

        int w = borderImage.getWidth();
        int h = borderImage.getHeight();
        int x, y;
        int acc = 0;

        y = h / 2;
        for (x = 0; x < w / 2; x++) {
            if (new Color(borderImage.getRGB(x, y), true).getAlpha() == 0) {
                acc += x;
                break;
            }
        }

        borderSizePx = acc;
        logger.ok("tested border size:", borderSizePx + "px");
        return acc;
    }

    public Path getOutputFile(String name) {
        try {
            if (!Files.exists(outputDir))
                Files.createDirectory(outputDir);
            Path to = outputDir.resolve(archiveName);
            archiveDir = to;
            if (!Files.exists(to))
                Files.createDirectory(to);
            Path f = to.resolve(name);
            return f;
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

}
