package org.cubewhy.lunarcn;

import org.cubewhy.launcher.LunarDownloader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("Get version artifact info")
public class TestGetArtifactInfo {
    @DisplayName("Version 1.8.9")
    @Test
    public void version189() throws IOException {
        System.out.println(LunarDownloader.getLunarArtifacts("1.8.9", "master", "lunar"));
    }
    
    @DisplayName("Version 1.12.2")
    @Test
    public void version1122() throws IOException {
        System.out.println(LunarDownloader.getLunarArtifacts("1.12.2", "master", "lunar"));
    }
}
