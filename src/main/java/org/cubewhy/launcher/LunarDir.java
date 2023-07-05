package org.cubewhy.launcher;

import java.io.File;

public class LunarDir {
    public static final File lunarDir = new File(System.getProperty("user.home"), ".lunarclient");

    public static void initLunarDir() {
        initDir(lunarDir); // Lunar配置目录
        initDir(lunarDir + "/offline/cn"); // 游戏目录
    }

    private static void initDir(File path) {
        if (!path.exists() || path.isFile()) {
            path.mkdirs();
        }
    }

    private static void initDir(String path) {
        initDir(new File(path));
    }
}
