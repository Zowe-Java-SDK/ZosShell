package zos.shell.utility;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.configuration.ConfigSingleton;
import zos.shell.constants.Constants;

public class DirectorySetup {

    private static final Logger LOG = LoggerFactory.getLogger(DirectorySetup.class);

    private static final String DIRECTORY_PATH_WINDOWS = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\";
    private static final String DIRECTORY_PATH_MAC = Constants.PATH_FILE_DIRECTORY_MAC + "/";
    private String directoryPath;
    private String fileNamePath;

    public void initialize(final String directoryName, final String fileName) {
        LOG.debug("*** initialize ***");
        final var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        final var configPath = configSettings != null ? configSettings.getDownloadPath() : null;
        if (SystemUtils.IS_OS_WINDOWS) {
            directoryPath = configPath != null ? configPath + (!configPath.endsWith("\\") ? "\\" : "") + directoryName :
                    DIRECTORY_PATH_WINDOWS + directoryName;
            fileNamePath = directoryPath + "\\" + fileName;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            directoryPath = configPath != null ? configPath + (!configPath.endsWith("/") ? "/" : "") + directoryName :
                    DIRECTORY_PATH_MAC + directoryName;
            fileNamePath = directoryPath + "/" + fileName;
        } else {
            throw new IllegalStateException(Constants.OS_ERROR);
        }
    }

    public String getDirectoryPath() {
        LOG.debug("*** getDirectoryPath ***");
        return directoryPath;
    }

    public String getFileNamePath() {
        LOG.debug("*** getFileNamePath ***");
        return fileNamePath;
    }

    @Override

    public String toString() {
        LOG.debug("*** toString ***");
        return "DirectorySetup{" +
                "directoryPath='" + directoryPath + '\'' +
                ", fileNamePath='" + fileNamePath + '\'' +
                '}';
    }

}