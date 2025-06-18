package zos.shell.service.localfile;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.controller.EnvVariableController;
import zos.shell.singleton.configuration.ConfigSingleton;

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

    private final EnvVariableController envVariableController;

    public LocalFileService(EnvVariableController envVariableController) {
        LOG.debug("*** LocalFileService ***");
        this.envVariableController = envVariableController;
    }

    public StringBuilder listFiles(final String target) {
        LOG.debug("*** listFiles ***");
        return getFiles(target);
    }

    private StringBuilder getFiles(final String target) {
        LOG.debug("*** getFiles ***");
        var result = new StringBuilder();
        String path;
        String downloadPath = envVariableController.getValueByEnv("DOWNLOAD_PATH");
        var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        var configPath = configSettings != null ? downloadPath : "";
        var targetValue = target != null && !target.isBlank() ? target : "";
        if (SystemUtils.IS_OS_WINDOWS) {
            var configPathValue = configPath + (!configPath.endsWith("\\") ? "\\" : "") + targetValue;
            path = !configPathValue.isBlank() ? configPathValue : DIRECTORY_PATH_WINDOWS + targetValue;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            var configPathValue = configPath + (!configPath.endsWith("/") ? "/" : "") + targetValue;
            path = !configPathValue.isBlank() ? configPathValue : DIRECTORY_PATH_MAC + targetValue;
        } else {
            return result.append(Constants.OS_ERROR);
        }
        if (path.isBlank()) {
            return result.append(Constants.NO_FILES);
        }
        var files = Optional.ofNullable(new File(path).listFiles());
        var results = new ArrayList<>(List.of(path + ":"));
        results.addAll(Stream.of(files.orElse(new File[]{})).map(File::getName).sorted().collect(Collectors.toList()));
        results.forEach(i -> result.append(i).append("\n"));
        return result;
    }

}
