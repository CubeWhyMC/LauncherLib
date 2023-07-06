package org.cubewhy.launcher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cubewhy.launcher.game.MinecraftArgs;
import org.cubewhy.lunarcn.JavaAgent;

import java.io.IOException;
import java.util.*;

public class LunarClient {
    /**
     * 获取LunarClient主类
     *
     * @return mainClass
     */
    public static String getMainClass() {
        return "com.moonsworth.lunar.genesis.Genesis";
    }

    /**
     * 获取默认JVM参数
     *
     * @return jvm args
     */
    public static ArrayList<String> getDefaultJvmArgs(String version, String module, String branch) throws IOException {
        ArrayList<String> out = new ArrayList<>();
        for (JsonElement arg : Objects.requireNonNull(LunarDownloader.getVersionJson(version, branch, module))
                .getAsJsonObject()
                .getAsJsonObject("jre")
                .getAsJsonArray("extraArguments")) {
            out.add(arg.getAsString());
        }
        return out;
    }

    /**
     * 获取LunarClient主类
     *
     * @param version 游戏版本
     * @return mainClass
     */
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

    /**
     * 拼接参数
     *
     * @param version     游戏版本
     * @param module      启用的模块
     * @param branch      分支
     * @param baseDir     游戏工件所在的目录
     * @param java        Java可执行文件
     * @param jvmArgs     Java虚拟机参数
     * @param programArgs 程序参数
     * @param agents      要添加的Java助理
     */
    public static String getArgs(String version, String module, String branch, String baseDir, MinecraftArgs minecraftArgs, String java, String[] jvmArgs, String[] programArgs, JavaAgent[] agents) throws IOException {
        ArrayList<String> args = new ArrayList<>();
        args.add(java); // Java可执行文件
        ArrayList<String> jvmArgsList = new ArrayList<>(Arrays.asList(jvmArgs));
        jvmArgsList.addAll(getDefaultJvmArgs(version, module, branch));
        args.add(String.join(" ", jvmArgsList)); // JVM参数

        // JavaAgents
        for (JavaAgent agent :
                agents) {
            args.add(agent.getJvmArgs()); // pre JavaAgent JVM args
        }
        // ClassPaths
        JsonObject artifacts = LunarDownloader.getLunarArtifacts(version, branch, module);
        StringBuilder classpath = new StringBuilder("-cp ");
        StringBuilder ichorPath = new StringBuilder("--ichorExternalFiles ");
        for (JsonElement artifact :
                Objects.requireNonNull(LunarDownloader.getVersionJson(version, branch, module))
                        .getAsJsonObject()
                        .getAsJsonObject("launchTypeData")
                        .getAsJsonArray("artifacts")) {
            if (artifact.getAsJsonObject().get("type").getAsString().equals("CLASS_PATH")) {
                // is ClassPath
                classpath.append(baseDir).append("/")
                        .append(artifact.getAsJsonObject().get("name").getAsString())
                        .append(";");
            } else if (artifact.getAsJsonObject().get("type").getAsString().equals("EXTERNAL_FILE")) {
                // is external file
                ichorPath.append(baseDir).append("/")
                        .append(artifact.getAsJsonObject().get("name").getAsString())
                        .append(",");
            }
        }
        args.add(classpath.toString()); // classPath
        args.add(getMainClass(version)); // 主类
        // Minecraft参数
        boolean ichorEnabled = getIChorState(version, branch, module);

        args.add("--version" + version); // what version will lunarClient inject
        args.add("--accessToken 0");
        args.add("--userProperties {}");
        args.add("--launcherVersion 2.15.1");
        args.add("--workingDirectory " + baseDir);
        args.add("--classpathDir " + baseDir);
        args.add("--width " + minecraftArgs.width);
        args.add("--height " + minecraftArgs.height);
        args.add("--gameDir " + minecraftArgs.gameDir);
        args.add("--assetIndex " + version.substring(0, version.lastIndexOf("."))); // 资源Index
        if (ichorEnabled) {
            args.add("--ichorClassPath " + classpath.toString().replace(";", ","));
            args.add(ichorPath.toString());
        }
        return String.join(" ", args);
    }

    /**
     * 获取ICHOR状态
     */
    private static boolean getIChorState(String version, String branch, String module) throws IOException {
        try {
            return LunarDownloader.getVersionJson(version)
                    .getAsJsonObject()
                    .getAsJsonObject("launchTypeData")
                    .get("ichor").getAsBoolean();
        } catch (IOException e) {
            return true;
        }
    }

    public static void main(String[] args) throws IOException {
        MinecraftArgs minecraftArgs = new MinecraftArgs("%APPDATA%/.minecraft", "C:\\Users\\nkwjg\\.lunarclient\\textures", 600, 600);
        String game = getArgs("1.8.9", "lunar", "master", LunarDir.lunarDir + "/cn", minecraftArgs, "java.exe", new String[]{}, new String[]{}, new JavaAgent[]{});
        System.out.println(game);
    }
}
