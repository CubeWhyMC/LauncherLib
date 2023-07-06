package org.cubewhy.launcher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Response;
import org.cubewhy.launcher.utils.HttpUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class MinecraftDownloader {
    public static final String launcherMetaApi = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";

    /**
     * 获取所有游戏版本
     * @return metadata
     * */
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
     * @param version Minecraft 版本
     * @return version json
     * */
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

}
