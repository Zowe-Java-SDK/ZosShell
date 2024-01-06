package zos.shell.utility;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public static void openFileLocation(String filePath) {
        LOG.debug("*** openFileLocation ***");
        if (filePath == null) {
            return;
        }

        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                Runtime.getRuntime().exec("explorer.exe /select, " + filePath);
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                final var arr = filePath.split("/");
                final var str = new StringBuilder();
                for (var i = 0; i < arr.length - 1; i++) {
                    str.append(arr[i]).append("/");
                }
                Runtime.getRuntime().exec("/usr/bin/open " + str);
            }
        } catch (IOException ignored) {
        }
    }

    public static void writeTextFile(String content, String directoryPath, String fileNamePath) throws IOException {
        LOG.debug("*** writeTextFile ***");
        Files.createDirectories(Paths.get(directoryPath));
        Files.write(Paths.get(fileNamePath), content.getBytes());
    }

}
