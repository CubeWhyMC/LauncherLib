package org.cubewhy.lunarcn;

import org.cubewhy.launcher.LunarDownloader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("Get version meta info")
public class TestGetMetaInfo {
    @DisplayName("Version 1.8.9")
    @Test
    public void version189() throws IOException {
        System.out.println(LunarDownloader.getSubVersion("1.8.9"));
    }

    @DisplayName("Version 1.12.2")
    @Test
    public void version1122() throws IOException {
        System.out.println(LunarDownloader.getSubVersion("1.12.2"));
    }
}
