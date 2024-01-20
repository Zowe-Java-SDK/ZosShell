package zos.shell.service.path;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.configuration.ConfigSingleton;
import zos.shell.constants.Constants;
import zos.shell.utility.DsnUtil;

public class PathService {

    private static final Logger LOG = LoggerFactory.getLogger(PathService.class);

    private static final String DIRECTORY_PATH_WINDOWS = Constants.DEFAULT_DOWNLOAD_PATH_WINDOWS + "\\";

    private static final String DIRECTORY_PATH_MAC = Constants.DEFAULT_DOWNLOAD_PATH_MAC + "/";

    private String path;
    private String pathWithFile;
    private final String dataset;
    private final String target;

    public PathService(final String dataset, final String target) {
        LOG.debug("*** PathService dataset target ***");
        this.dataset = dataset;
        this.target = target;
        this.initialize();
    }

    public PathService(final String target) {
        LOG.debug("*** PathService target ***");
        if (DsnUtil.isDataSet(target)) {
            this.dataset = Constants.SEQUENTIAL_DIRECTORY_LOCATION;
        } else {
            throw new IllegalArgumentException("Expected sequential dataset");
        }
        this.target = target;
        this.initialize();
    }

    public void initialize() {
        LOG.debug("*** initialize ***");
        var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        String configPath = configSettings != null ? configSettings.getDownloadPath() : null;
        if (SystemUtils.IS_OS_WINDOWS) {
            path = configPath != null ? configPath + (!configPath.endsWith("\\") ? "\\" : "") + dataset
                    : DIRECTORY_PATH_WINDOWS + dataset;
            pathWithFile = path + "\\" + target;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            path = configPath != null ? configPath + (!configPath.endsWith("/") ? "/" : "") + dataset
                    : DIRECTORY_PATH_MAC + dataset;
            pathWithFile = path + "/" + target;
        } else {
            throw new IllegalStateException(Constants.OS_ERROR);
        }
    }

    public String getPath() {
        LOG.debug("*** getPath ***");
        return path;
    }

    public String getPathWithFile() {
        LOG.debug("*** getPathWithFile ***");
        return pathWithFile;
    }

    @Override
    public String toString() {
        return "DirectorySetup{" +
                "path='" + path + '\'' +
                ", pathWithFile='" + pathWithFile + '\'' +
                ", dataset='" + dataset + '\'' +
                ", target='" + target + '\'' +
                '}';
    }

}