package org.cubewhy.launcher;

import com.google.gson.JsonElement;
import org.cubewhy.launcher.game.MinecraftArgs;
import org.cubewhy.launcher.utils.ZipUtils;
import org.cubewhy.lunarcn.JavaAgent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class LunarClient {
    /**
     * Get main-class of LunarClient
     *
     * @return mainClass
     */
    public static String getMainClass() {
        return "com.moonsworth.lunar.genesis.Genesis";
    }

    /**
     * Get default JVM args
     *
     * @return jvm args
     */
    public static ArrayList<String> getDefaultJvmArgs(String version, String module, String branch, String baseDir) throws IOException {
        ArrayList<String> out = new ArrayList<>();
        for (JsonElement arg : Objects.requireNonNull(LunarDownloader.getVersionJson(version, branch, module))
                .getAsJsonObject()
                .getAsJsonObject("jre")
                .getAsJsonArray("extraArguments")) {
            if (arg.getAsString().equals("-Djna.boot.library.path=natives")) {
                out.add("-Djna.boot.library.path=" + baseDir + "/" + "natives");
                continue;
            }
            out.add(arg.getAsString());
        }
        out.add("-Djava.library.path=" + baseDir + "/" + "natives");
        return out;
    }

    /**
     * Get LunarClient main-class (online)
     *
     * @param version Minecraft version
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
     * @param version       Minecraft version
     * @param module        addon
     * @param branch        branch
     * @param baseDir       Game artifacts dir
     * @param java          Java executable
     * @param jvmArgs       JVM args
     * @param programArgs   Game args
     * @param minecraftArgs Minecraft args
     * @param agents        JavaAgents
     * @param setupNatives  Unzip Natives
     */
    public static String getArgs(String version, String module, String branch, String baseDir, MinecraftArgs minecraftArgs, String java, String[] jvmArgs, String[] programArgs, JavaAgent[] agents, boolean setupNatives) throws IOException {
        ArrayList<String> args = new ArrayList<>();
        args.add(java); // Java可执行文件
        ArrayList<String> jvmArgsList = new ArrayList<>(Arrays.asList(jvmArgs));
        jvmArgsList.addAll(getDefaultJvmArgs(version, module, branch, baseDir));
        args.add(String.join(" ", jvmArgsList)); // JVM参数

        // JavaAgents
        for (JavaAgent agent :
                agents) {
            args.add(agent.getJvmArgs()); // pre JavaAgent JVM args
        }
        // ClassPaths
        StringBuilder classpath = new StringBuilder("-cp ");
        StringBuilder ichorPath = new StringBuilder("--ichorExternalFiles ");
        StringBuilder lunarClasspath = new StringBuilder("--ichorClassPath ");
        String nativesZip = "natives.zip"; // Not default value, just for init
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
                lunarClasspath.append(artifact.getAsJsonObject().get("name").getAsString())
                        .append(",");
            } else if (artifact.getAsJsonObject().get("type").getAsString().equals("EXTERNAL_FILE")) {
                // is external file
                ichorPath.append(artifact.getAsJsonObject().get("name").getAsString())
                        .append(",");
            } else if (artifact.getAsJsonObject().get("type").getAsString().equals("NATIVES")) {
                // natives
                nativesZip = artifact.getAsJsonObject().get("name").getAsString();
            }
        }
        args.add(classpath.toString()); // classPath
        args.add(getMainClass(version)); // 主类

        if (setupNatives) {
            // unzip Natives
            unzipNatives(new File(baseDir, nativesZip), baseDir);
        }

        // Minecraft参数
        boolean ichorEnabled = getIChorState(version, branch, module);

        args.add("--version " + version); // what version will lunarClient inject
        args.add("--accessToken 0");
        args.add("--userProperties {}");
        args.add("--launcherVersion 2.15.1");
        args.add("--hwid PUBLIC-HWID");
        args.add("--installationId INSTALL-ID");
        args.add("--workingDirectory " + baseDir);
        args.add("--classpathDir " + baseDir);
        args.add("--width " + minecraftArgs.width);
        args.add("--height " + minecraftArgs.height);
        args.add("--gameDir " + minecraftArgs.gameDir);
        args.add("--texturesDir " + minecraftArgs.texturesDir);
        if (minecraftArgs.server != null) {
            args.add("--server " + minecraftArgs.server); // Join server after launch
        }
        args.add("--assetIndex " + version.substring(0, version.lastIndexOf("."))); // 资源Index
        if (ichorEnabled) {
            args.add(lunarClasspath.toString());
            args.add(ichorPath.toString());
        }
        if (programArgs.length > 0) {
            args.add(String.join("", programArgs));
        }
        return String.join(" ", args);
    }

    /**
     * Get ICHOR state
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

    /**
     * Unzip natives
     *
     * @param nativesZip natives zip
     */
    public static void unzipNatives(File nativesZip, String baseDir) throws IOException {
        File dir = new File(baseDir, "natives");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ZipUtils.unZip(nativesZip, dir);
    }

    /**
     * Launch the game
     *
     * @param version       Minecraft version
     * @param module        LunarClient addon
     * @param branch        LunarClient branch
     * @param baseDir       游戏工件所在的目录
     * @param java          Java executable
     * @param jvmArgs       JVM args
     * @param programArgs   Game args
     * @param minecraftArgs Minecraft args
     * @param agents        JavaAgents
     * @return Game process
     */
    public static Process launch(String version, String module, String branch, String baseDir, MinecraftArgs minecraftArgs, String java, String[] jvmArgs, String[] programArgs, JavaAgent[] agents) throws IOException {
        String args = getArgs(version, module, branch, baseDir, minecraftArgs, java, jvmArgs, programArgs, agents, true);
        return Runtime.getRuntime().exec(args);
    }
}
