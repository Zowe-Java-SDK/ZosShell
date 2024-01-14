package zos.shell.service.localfile;

import org.apache.commons.lang3.SystemUtils;
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

public class LocalFileService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileService.class);

    private static final String DIRECTORY_PATH_WINDOWS = Constants.DEFAULT_DOWNLOAD_PATH_WINDOWS + "\\";
    private static final String DIRECTORY_PATH_MAC = Constants.DEFAULT_DOWNLOAD_PATH_MAC + "/";

    public LocalFileService() {
        LOG.debug("*** LocalFileService ***");
    }

    public StringBuilder listFiles(final String target) {
        LOG.debug("*** listFiles ***");
        return getFiles(target);
    }

    private StringBuilder getFiles(final String target) {
        LOG.debug("*** getFiles ***");
        StringBuilder result = new StringBuilder();
        String path;
        final var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        final var configPath = configSettings != null ? configSettings.getDownloadPath() : "";
        final String targetValue = target != null && !target.isBlank() ? target : "";
        if (SystemUtils.IS_OS_WINDOWS) {
            final String configPathValue = configPath + (!configPath.endsWith("\\") ? "\\" : "") + targetValue;
            path = !configPathValue.isBlank() ? configPathValue : DIRECTORY_PATH_WINDOWS + targetValue;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            final String configPathValue = configPath + (!configPath.endsWith("/") ? "/" : "") + targetValue;
            path = !configPathValue.isBlank() ? configPathValue : DIRECTORY_PATH_MAC + targetValue;
        } else {
            return result.append(Constants.OS_ERROR);
        }
        if (path.isBlank()) {
            return result.append(Constants.NO_FILES);
        }
        final var files = Optional.ofNullable(new File(path).listFiles());
        final var results = new ArrayList<>(List.of(path + ":"));
        results.addAll(Stream.of(files.orElse(new File[]{})).map(File::getName).sorted().collect(Collectors.toList()));
        results.forEach(i -> result.append(i).append("\n"));
        return result;
    }

}
