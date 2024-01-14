package zos.shell.service.localfile;

import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.configuration.ConfigSingleton;
import zos.shell.constants.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalFileCmd {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileCmd.class);

    private static final String DIRECTORY_PATH_WINDOWS = Constants.DEFAULT_DOWNLOAD_PATH_WINDOWS + "\\";
    private static final String DIRECTORY_PATH_MAC = Constants.DEFAULT_DOWNLOAD_PATH_MAC + "/";

    public static void listFiles(final TextTerminal<?> terminal, final String dataSet) {
        LOG.debug("*** listFiles ***");
        final var files = getFiles(terminal, dataSet);
        if (files.isEmpty()) {
            terminal.println(Constants.NO_FILES);
            return;
        }
        files.forEach(terminal::println);
    }

    private static List<String> getFiles(final TextTerminal<?> terminal, final String dataset) {
        LOG.debug("*** getFiles ***");
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC_OSX) {
            return new ArrayList<>();
        }
        String path;
        final var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        final var configPath = configSettings != null ? configSettings.getDownloadPath() : "";
        final String datasetValue = dataset != null && !dataset.isBlank() ? dataset : "";
        if (SystemUtils.IS_OS_WINDOWS) {
            final String configPathValue = configPath + (!configPath.endsWith("\\") ? "\\" : "") + datasetValue;
            path = !configPathValue.isBlank() ? configPathValue : DIRECTORY_PATH_WINDOWS + datasetValue;
        } else {
            final String configPathValue = configPath + (!configPath.endsWith("/") ? "/" : "") + datasetValue;
            path = !configPathValue.isBlank() ? configPathValue : DIRECTORY_PATH_MAC + datasetValue;
        }
        if (!path.isBlank()) {
            terminal.println(path + ":");
        } else {
            return new ArrayList<>();
        }
        final var files = Optional.ofNullable(new File(path).listFiles());
        return Stream.of(files.orElse(new File[]{})).map(File::getName).sorted().collect(Collectors.toList());
    }

}
