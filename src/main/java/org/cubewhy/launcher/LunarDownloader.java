package org.cubewhy.launcher;

import com.google.gson.*;
import okhttp3.Response;
import org.cubewhy.launcher.utils.FileUtils;
import org.cubewhy.launcher.utils.HttpUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LunarDownloader {
    public static final String api = "https://api.lunarclientprod.com/launcher/launch";
    public static final String metadataApi = "https://api.lunarclientprod.com/launcher/metadata?launcher_version=2.15.1";

    /**
     * 请求LunarClient的API并获取Lunar文件的下载地址
     *
     * @param version Minecraft version
     * @param branch  Branch of LunarClient
     * @param module  addon
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
     * @param version Minecraft version
     * @param module  addons
     * @return JSON of version
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
     * Get LunarLauncher metadata
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
     * Get support versions
     *
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
     * Get information of subversion
     *
     * @param version 子版本
     * @return version json
     */
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
     * Get support addons
     *
     * @param version Minecraft version
     * @return Module List
     */
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
     * Get a list of LunarClient Artifacts
     *
     * @param version Minecraft version
     * @param branch  Branch of LunarClient
     * @param module  addon
     */

    public static JsonObject getLunarArtifacts(String version, String branch, String module) throws IOException {
        JsonObject out = new JsonObject();

        JsonObject versionJson = Objects.requireNonNull(getVersionJson(version, branch, module)).getAsJsonObject();
        JsonObject launchTypeData = versionJson.getAsJsonObject("launchTypeData");
        JsonArray artifacts = launchTypeData.getAsJsonArray("artifacts");

        for (JsonElement artifact :
                artifacts) {
            JsonObject info = new JsonObject();
            String key = artifact.getAsJsonObject().get("name").getAsString();
            JsonElement url = artifact.getAsJsonObject().get("url");
            JsonElement sha1 = artifact.getAsJsonObject().get("sha1");
            JsonElement type = artifact.getAsJsonObject().get("type");
            info.add("url", url);
            info.add("sha1", sha1);
            info.add("type", type);
            out.add(key, info);
        }

        return out;
    }

    /**
     * 自动下载Lunar的工件
     *
     * @param downloadPath download path
     * @param artifacts    artifacts list
     * @param update       update LunarClient
     */
    public static void downloadLunarArtifacts(File downloadPath, JsonObject artifacts, boolean update) {
        if (!downloadPath.exists()) {
            downloadPath.mkdirs();
        }
        for (Map.Entry<String, JsonElement> keySet : artifacts.entrySet()) {
            String fileName = keySet.getKey();
            JsonObject info = keySet.getValue().getAsJsonObject();
            String url = info.get("url").getAsString();
            File file = new File(downloadPath, fileName);
            if (file.exists() && update) {
                String remoteSh1 = info.get("sh1").getAsString();
                String sh1 = FileUtils.sha1(file);
                if (remoteSh1.equals(sh1)) {
                    continue; // no need update
                }
            }
            try {
                byte[] fileBytes = HttpUtils.download(url);
                try (FileOutputStream stream = new FileOutputStream(downloadPath + "/" + fileName)) {
                    if (fileBytes != null) {
                        stream.write(fileBytes);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Download artifacts of LunarClient
     *
     * @param downloadPath Download path
     * @param artifacts    artifact list
     * @param update       Update LunarClient
     */
    public static void downloadLunarArtifacts(String downloadPath, JsonObject artifacts, boolean update) {
        downloadLunarArtifacts(new File(downloadPath), artifacts, update);
    }

    /**
     * Get the textures' index of LunarClient
     *
     * @param version version of Minecraft
     * @param branch  branch of LunarClient
     * @param addon   LunarClient Addon
     * @return textures' index
     */
    public static JsonElement getLunarTexturesIndex(String version, String branch, String addon) throws IOException {
        JsonObject versionJson = Objects.requireNonNull(getVersionJson(version, branch, addon)).getAsJsonObject();
        String indexUrl = versionJson.getAsJsonObject("textures").get("indexUrl").getAsString();
        // get index json
        String baseUrl = getLunarTexturesBaseUrl(version, branch, addon);
        try (Response response = HttpUtils.get(indexUrl).execute()) {
            if (response.body() != null) {
                // parse
                JsonObject jsonObject = new JsonObject();
                for (String s : response.body().string().split("\n")) {
                    // filename hashcode
                    jsonObject.addProperty(baseUrl + s.split(" ")[0], s.split(" ")[1]);
                }
                return jsonObject;
            }
        }
        return null;
    }

    @NotNull
    @Contract(pure = true)
    public static String getLunarTexturesBaseUrl() {
        return "https://textures.lunarclientcdn.com/file/";
    }

    public static String getLunarTexturesBaseUrl(String version, String branch, String module) throws IOException {
        JsonObject versionJson = Objects.requireNonNull(getVersionJson(version, branch, module)).getAsJsonObject();
        return versionJson.get("baseUrl").getAsString();
    }

    /**
     * Download Textures of LunarClient
     *
     * @param downloadPath Where save files
     * @param index        Textures index
     */
    public static void downloadLunarTextures(File downloadPath, JsonElement index) {
        downloadLunarTextures(downloadPath, index, true);
    }

    /**
     * Download Textures of LunarClient
     *
     * @param downloadPath Where save files
     * @param index        Textures index
     * @param update       update resources
     */
    public static void downloadLunarTextures(File downloadPath, JsonElement index, boolean update) {
        for (Map.Entry<String, JsonElement> keySet : index.getAsJsonObject().entrySet()) {
            String[] urlFull = keySet.getKey().split("/");
            String fileName = urlFull[urlFull.length - 1];
            String url = keySet.getKey();
            File file = new File(downloadPath, fileName);
            if (file.exists() && update) {
                String sh1 = keySet.getValue().getAsString();
                if (sh1.equals(FileUtils.sha1(file))) {
                    continue;
                }
            }
            try {
                byte[] fileBytes = HttpUtils.download(url);
                try (FileOutputStream stream = new FileOutputStream(downloadPath + "/" + fileName)) {
                    if (fileBytes != null) {
                        stream.write(fileBytes);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get is installed
     *
     * @param version    Minecraft version
     * @param branch     LunarClient branch
     * @param module     addons
     * @param installDir Lunar install dir
     */
    public static boolean hasInstalled(String version, String branch, String module, File installDir) throws IOException {
        JsonObject artifacts = Objects.requireNonNull(getLunarArtifacts(version, branch, module)).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : artifacts.entrySet()) {
            if (!(new File(installDir, entry.getKey()).isFile())) {
                return false;
            }
        }
        return true;
    }
}
