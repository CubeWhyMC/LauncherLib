package org.cubewhy.launcher.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    /**
     * 解压缩Zip存档
     * @param input zip存档
     * @param outputDir 解压缩目标目录
     * */
    public static void unZip(File input, File outputDir) throws IOException {
        ZipFile zipfile = new ZipFile(input);
        ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(input.toPath()));

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            File out = new File(outputDir, entry.getName());
            if (entry.isDirectory()) {
                out.mkdirs();
            } else {
                out.createNewFile();
                InputStream entryInputStream = zipfile.getInputStream(entry);
                try (FileOutputStream fileOutPutStream = new FileOutputStream(out)) {
                    int b;
                    while ((b = entryInputStream.read()) != -1) {
                        fileOutPutStream.write(b);
                    }
                }
            }
        }
    }
}
