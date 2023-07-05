package org.cubewhy.launcher;

import com.google.gson.*;
import okhttp3.Response;
import org.cubewhy.launcher.utils.HttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LunarDownloader {
    public static final String api = "https://api.lunarclientprod.com/launcher/launch";
    public static final String metadataApi = "https://api.lunarclientprod.com/launcher/metadata?launcher_version=2.15.1";

    /**
     * 请求LunarClient的API并获取Lunar文件的下载地址
     *
     * @param version Minecraft版本
     * @param branch  LunarClient的分支
     * @param module addon
     * @return JSON
     */
    public static JsonElement getVersionJson(String version, String branch, String module) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("hwid", "HWID-PUBLIC");
        json.addProperty("hwid-private", "HWID-PRIVATE");
        json.addProperty("installation_id", "INSTALL_ID");
        json.addProperty("os", "win32");
        json.addProperty("arch", "x64");
        json.addProperty("os_release", "19045.3086");
        json.addProperty("launcher_version", "2.15.1");
        json.addProperty("launch_type", "lunar");
        json.addProperty("version", version);
        json.addProperty("branch", branch);
        json.addProperty("module", module);
        // 开始请求
        try (Response response = HttpUtils.post(api, new Gson().toJson(json)).execute()) {
            if (response.body() != null) {
                return JsonParser.parseString(response.body().string());
            }
        }
        return null;
    }

    /**
     * 请求LunarClient的API并获取Lunar文件的下载地址
     *
     * @param version Minecraft版本
     * @param module  要启用的模块
     * @return JSON
     */
    public static JsonElement getVersionJson(String version, String module) throws IOException {
        return getVersionJson(version, "master", module);
    }

    /**
     * 请求LunarClient的API并获取Lunar文件的下载地址
     *
     * @param version Minecraft版本
     * @return JSON
     */
    public static JsonElement getVersionJson(String version) throws IOException {
        return getVersionJson(version, "master", "lunar");
    }

    /**
     * 获取Lunar metadata
     *
     * @return Metadata Json
     */

    public static JsonElement getMetadata() throws IOException {
        try (Response response = HttpUtils.get(metadataApi).execute()) {
            if (response.body() != null) {
                return JsonParser.parseString(response.body().string());
            }
        }
        return null;
    }


    /**
     * 获取支持的版本
     * @return Support versions list
     */
    public static List<String> getSupportVersions() throws IOException {
        JsonElement metadata = getMetadata();
        List<String> versions = new ArrayList<>();
        JsonArray versionsJson = Objects.requireNonNull(metadata).getAsJsonObject().getAsJsonArray("versions");
        for (JsonElement version :
                versionsJson) {
            String versionId = version.getAsJsonObject().get("id").getAsString();
            if (version.getAsJsonObject().has("subversions")) {
                for (JsonElement subVersion :
                        version.getAsJsonObject().get("subversions").getAsJsonArray()) {
                    versions.add(subVersion.getAsJsonObject().get("id").getAsString());
                }
            } else {
                versions.add(versionId);
            }
        }
        return versions;
    }

    /**
     * 获取子版本数据
     * @param version 子版本
     * @return version json
     * */
    public static JsonObject getSubVersion(String version) throws IOException {
        JsonObject metadata = Objects.requireNonNull(getMetadata()).getAsJsonObject();
        for (JsonElement version1 : metadata.get("versions").getAsJsonArray()) {
            if (version.contains(version1.getAsJsonObject().get("id").getAsString())) {
                for (JsonElement subVersion : version1.getAsJsonObject().get("subversions").getAsJsonArray()) {
                    if (subVersion.getAsJsonObject().get("id").getAsString().equals(version)) {
                        return subVersion.getAsJsonObject();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取支持的模块
     * @param version Minecraft 版本
     * @return Module List
     * */
    public static List<String> getSupportModules(String version) throws IOException {
        List<String> modules = new ArrayList<>();
//        boolean isSubVersion = StringUtils.count(version, '.') >= 2;
        JsonObject version1 = getSubVersion(version);
        JsonArray modulesJson = Objects.requireNonNull(version1).getAsJsonArray("modules");
        for (JsonElement moduleJson :
                modulesJson) {
            modules.add(moduleJson.getAsJsonObject().get("id").getAsString());
        }
        return modules;
    }

    /**
     * 获取Lunar的工件
     * @param version Minecraft版本
     * @param branch 分支
     * @param module addon
     * */

    public static JsonObject getLunarArtifacts(String version, String branch, String module) throws IOException {
        JsonObject out = new JsonObject();

        JsonObject versionJson = Objects.requireNonNull(getVersionJson(version, branch, module)).getAsJsonObject();
        JsonObject launchTypeData = versionJson.getAsJsonObject("launchTypeData");
        JsonArray artifacts = launchTypeData.getAsJsonArray("artifacts");

        for (JsonElement artifact :
                artifacts) {
            String key = artifact.getAsJsonObject().get("name").getAsString();
            JsonElement value = artifact.getAsJsonObject().get("url");
            out.add(key, value);
        }

        return out;
    }
}
