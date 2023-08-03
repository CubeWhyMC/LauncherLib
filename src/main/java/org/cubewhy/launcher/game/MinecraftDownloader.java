package org.cubewhy.launcher.game;

import com.google.gson.*;
import okhttp3.Response;
import org.cubewhy.launcher.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MinecraftDownloader {
    public static final String launcherMetaApi = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";

    /**
     * 获取所有游戏版本
     *
     * @return metadata
     */
    @Nullable
    public static JsonObject getLauncherMeta() throws IOException {
        try (Response response = HttpUtils.get(launcherMetaApi).execute()) {
            if (response.body() != null) {
                return JsonParser.parseString(response.body().string()).getAsJsonObject();
            }
        }
        return null;
    }

    /**
     * 获取版本JSON
     *
     * @param version Minecraft 版本
     * @return version json
     */
    @Nullable
    public static JsonObject getVersionJson(String version) throws IOException {
        String jsonUrl = null;

        JsonObject launcherMeta = getLauncherMeta();
        JsonArray versions = Objects.requireNonNull(launcherMeta).getAsJsonArray("versions");
        for (JsonElement version1 : versions) {
            if (version1.getAsJsonObject().get("id").getAsString().equals(version)) {
                jsonUrl = version1.getAsJsonObject().get("url").getAsString();
            }
        }

        if (jsonUrl == null) {
            return null;
        }

        try (Response response = HttpUtils.get(jsonUrl).execute()) {
            if (response.body() != null) {
                return JsonParser.parseString(response.body().string()).getAsJsonObject();
            }
        }
        return null;
    }

    /**
     * 获取已发布的版本
     *
     * @param release  结果是否包含正式版
     * @param snapshot 结果是否包含测试版
     * @param oldAlpha 结果是否包含远古版本
     */
    @NotNull
    public static ArrayList<String> getVersions(boolean release, boolean snapshot, boolean oldAlpha) throws IOException {
        JsonObject metadata = getLauncherMeta();
        JsonArray versions = Objects.requireNonNull(metadata).getAsJsonArray("versions");

        ArrayList<String> out = new ArrayList<>();

        for (JsonElement versionJson :
                versions) {
            switch (versionJson.getAsJsonObject().get("type").getAsString()) {
                case "release":
                    if (!release) {
                        continue;
                    }
                    break;
                case "old_alpha":
                    if (!oldAlpha) {
                        continue;
                    }
                    break;
                case "snapshot":
                    if (!snapshot) {
                        continue;
                    }
                    break;
            }
            out.add(versionJson.getAsJsonObject().get("id").getAsString());
        }
        return out;
    }

    /**
     * Get all Minecraft versions
     */
    @NotNull
    public static ArrayList<String> getVersions() throws IOException {
        return getVersions(true, true, true);
    }

    /**
     * Get Asset Index of the version
     *
     * @param version Minecraft version
     */
    @Nullable
    public static HashMap<String, JsonObject> getAssetIndex(String version) throws IOException {
        JsonObject versionJson = getVersionJson(version);
        JsonObject ai = Objects.requireNonNull(versionJson).getAsJsonObject("assetIndex");
        String name = ai.get("id").getAsString();
        String jsonUrl = ai.get("url").getAsString();
        try (Response response = HttpUtils.get(jsonUrl).execute()) {
            if (response.body() != null) {
                JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                HashMap<String, JsonObject> map = new HashMap<>();
                map.put(name, json);
                return map;
            }
        }
        return null;
    }

    /**
     * Download version JSON
     *
     * @param version Minecraft version
     * @param outFile 目标文件
     */
    public static void downloadVersionJson(String version, @NotNull File outFile) throws IOException {
        JsonObject versionJson = getVersionJson(version);
        Gson gson = new Gson();
        String json = gson.toJson(versionJson);
        if (!outFile.exists()) {
            outFile.createNewFile();
        }

        try (FileWriter fileOutputStream = new FileWriter(outFile)) {
            fileOutputStream.write(json);
        }
    }

    /**
     * Download version JSON
     *
     * @param version Minecraft version
     * @param outFile path
     */
    public static void downloadVersionJson(String version, String outFile) throws IOException {
        downloadVersionJson(version, new File(outFile));
    }

    /**
     * Download Minecraft depends
     *
     * @param version    Minecraft version
     * @param libraryDir library dir
     */
    public static void downloadLibrariesOnline(String version, File libraryDir) throws IOException {
        JsonObject versionJson = getVersionJson(version);
        JsonArray libraries = Objects.requireNonNull(versionJson).getAsJsonArray("libraries");
        for (JsonElement lib :
                libraries) {
            // String libName = lib.getAsJsonObject().get("name").getAsString();
            if (!lib.getAsJsonObject().getAsJsonObject("downloads").has("artifact")) {
                continue; // skip natives
            }
            JsonObject artifactInfo = lib.getAsJsonObject().getAsJsonObject("downloads").getAsJsonObject("artifact");
            String pathToLib = artifactInfo.get("path").getAsString();
            String url = artifactInfo.get("url").getAsString();
            // Start downloading
            byte[] bytes = HttpUtils.download(url);
            File realLibFile = new File(libraryDir, pathToLib);
            realLibFile.getParentFile().mkdirs(); // Create dirs
            realLibFile.createNewFile();
            try (FileOutputStream fileOutputStream = new FileOutputStream(realLibFile)) {
                if (bytes != null) {
                    fileOutputStream.write(bytes); // write file
                }
            }
        }
    }

    /**
     * Download Minecraft depends
     *
     * @param version    Minecraft version
     * @param libraryDir library dir
     */
    public static void downloadLibrariesOnline(String version, String libraryDir) throws IOException {
        downloadLibrariesOnline(version, new File(libraryDir));
    }
}
