package org.cubewhy.launcher.game;

public class MinecraftArgs {
    public final String gameDir;

    public final String texturesDir;
    public final int width;
    public final int height;

    public MinecraftArgs(String gameDir, String texturesDir, int width, int height) {
        this.gameDir = gameDir;
        this.texturesDir = texturesDir;
        this.width = width;
        this.height = height;
    }
}
