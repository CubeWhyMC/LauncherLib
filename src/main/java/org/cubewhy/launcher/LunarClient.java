package org.cubewhy.launcher;

import java.io.IOException;

public class LunarClient {
    /**
     * 获取LunarClient主类
     * @return mainClass
     * */
    public static String getMainClass() {
        return "com.moonsworth.lunar.genesis.Genesis";
    }

    /**
     * 获取LunarClient主类
     * @param version 游戏版本
     * @return mainClass
     * */
    public static String getMainClass(String version) {
        try {
            return LunarDownloader.getVersionJson(version)
                    .getAsJsonObject()
                    .getAsJsonObject("launchTypeData")
                    .get("mainClass").getAsString();
        } catch (IOException e) {
            return getMainClass();
        }
    }
}
