package org.cubewhy.launcher.game;

public class MinecraftArgs {
    public final String gameDir;

    public final String texturesDir;
    public final int width;
    public final int height;
    public final String server;

    public MinecraftArgs(String gameDir, String texturesDir, int width, int height, String server) {
        this.gameDir = gameDir;
        this.texturesDir = texturesDir;
        this.width = width;
        this.height = height;
        this.server = server;
    }

    public MinecraftArgs(String gameDir, String texturesDir, int width, int height) {
        this(gameDir, texturesDir, width, height, null);
    }
}
