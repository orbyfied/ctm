package com.github.orbyfied.ctm.process;

import com.github.orbyfied.util.IOUtil;
import com.github.orbyfied.logging.Logger;

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

            // TODO: check if they are the same size

            // ok
            logger.ok("successfully loaded images");

        } catch (IOException e) {
            logger.err("io error while loading images:", e);
        }

    }

    public void export() {
        logger.stage("export");

        // check
        if (borderSizePx <= 0) {
            logger.err("border size is negative or zero (", borderSizePx, ")");
            return;
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
        }
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
