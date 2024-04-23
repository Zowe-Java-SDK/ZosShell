package zos.shell.utility;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void openFileLocation(final String filePath) {
        LOG.debug("*** openFileLocation ***");
        if (filePath == null) {
            return;
        }

        var file = new File(filePath);
        try {
            LOG.info("canonical path {}", file.getCanonicalPath());
            if (SystemUtils.IS_OS_WINDOWS) {
                // open directory in explorer
                Runtime.getRuntime().exec("explorer.exe /select, " + file.getCanonicalPath());
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                var arr = file.getCanonicalPath().split("/");
                var str = new StringBuilder();
                if (file.isDirectory()) {
                    str.append(file.getCanonicalPath());
                } else { // is file remove file name and open directory only
                    for (var i = 0; i < arr.length - 1; i++) {
                        str.append(arr[i]).append("/");
                    }
                }
                Runtime.getRuntime().exec("/usr/bin/open " + str);
            }
        } catch (IOException ignored) {
        }
    }

    public static void writeTextFile(final String content, final String directoryPath,
                                     final String fileNamePath) throws IOException {
        LOG.debug("*** writeTextFile ***");
        Files.createDirectories(Paths.get(directoryPath));
        Files.write(Paths.get(fileNamePath), content.getBytes());
    }

    public static void writeBinaryFile(final InputStream input, final String directoryPath,
                                       final String fileNamePath) throws IOException {
        LOG.debug("*** writeBinaryFile ***");
        Files.createDirectories(Paths.get(directoryPath));
        FileUtils.copyInputStreamToFile(input, new File(fileNamePath));
    }

    public static String getTextStreamData(final InputStream inputStream) throws IOException {
        LOG.debug("*** getTextStreamData ***");
        if (inputStream != null) {
            var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, Constants.UTF8);
            inputStream.close();
            return writer.toString();
        }
        return null;
    }

}
