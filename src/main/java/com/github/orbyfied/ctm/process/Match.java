package com.github.orbyfied.ctm.process;

import com.github.orbyfied.logging.Logger;

import java.io.PrintWriter;
import java.nio.file.Path;

public class Match {

    private final Maker  maker;
    private Logger logger;
    public Match(Maker maker) {
        this.maker  = maker;
    }

    /* Properties. */

    public String matches;
    public String tileName;
    public String method = "ctm";
    public String meta   = null;
    public String tiles  = "0-46";
    public String connect = null;

    public Match withProperties(
            String matchId,
            String tileName,
            String meta,
            String connect
            ) {
        this.matches = matchId;
        this.tileName = tileName;
        this.meta     = meta;
        this.connect  = connect;
        return this;
    }

    public Match withProperties(
            String matchId,
            String tileName
    ) {
        this.matches = matchId;
        this.tileName = tileName;
        return this;
    }

    public Match withTiles(String tiles) {
        this.tiles = tiles;
        return this;
    }

    public Match withMethod(String method) {
        this.method = method;
        return this;
    }

    public Path getFile(Path infolder) {
        return infolder.resolve(tileName + ".properties");
    }

    public Match writeFile(Path path, PrintWriter writer) {

        // process data
        String matchMethod;
        try { Integer.parseInt(matches); matchMethod = "matchBlocks"; }
        catch (NumberFormatException e) { matchMethod = "matchTiles"; }

        // write comment
        writer.println("# Auto-generated by ConnectedTextureMaker by orbyfied");
        writer.println("# tile_name: " + tileName + ", matches: " + matches);
        writer.println();

        // write data
        writer.println(matchMethod + "=" + matches);
        writer.println("method=" + method);
        if (connect != null) writer.println("connect=" + connect);
        writer.println("tiles=" + tiles);
        if (meta != null) writer.println("metadata=" + meta);

        // return
        return this;
    }

}
