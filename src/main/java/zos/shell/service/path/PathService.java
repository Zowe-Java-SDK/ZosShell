package zos.shell.service.path;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.controller.EnvVariableController;
import zos.shell.singleton.ConnSingleton;
import zos.shell.utility.DsnUtil;

public class PathService {

    private static final Logger LOG = LoggerFactory.getLogger(PathService.class);

    private static final String DIRECTORY_PATH_WINDOWS = Constants.DEFAULT_DOWNLOAD_PATH_WINDOWS + "\\";

    private static final String DIRECTORY_PATH_MAC = Constants.DEFAULT_DOWNLOAD_PATH_MAC + "/";

    private final ConnSingleton connSingleton;
    private final EnvVariableController envVariableController;
    private String pathToDirectory;
    private String pathToDirectoryWithFileName;

    public PathService(final ConnSingleton connSingleton, final EnvVariableController envVariableController) {
        LOG.debug("*** PathService ***");
        this.connSingleton = connSingleton;
        this.envVariableController = envVariableController;
    }

    public void createPathsForMember(final String dataset, final String target) {
        LOG.debug("*** createPathsForMember ***");
        this.initialize(dataset, target);
    }

    public void createPathsForSequentialDataset(final String target) {
        LOG.debug("*** createPathsForSequentialDataset ***");
        if (DsnUtil.isDataset(target)) {
            this.initialize(Constants.SEQUENTIAL_DIRECTORY_LOCATION, target);
        } else {
            throw new IllegalArgumentException("Expected sequential dataset");
        }
    }

    private void initialize(final String dataset, final String target) {
        LOG.debug("*** initialize ***");
        var downloadPath = envVariableController.getValueByEnv("DOWNLOAD_PATH");

        if (SystemUtils.IS_OS_WINDOWS) {
            pathToDirectory = !downloadPath.isBlank() ? downloadPath +
                    (!downloadPath.endsWith("\\") ? "\\" : "") +
                    connSingleton.getCurrZosConnection().getHost() + "\\" + dataset :
                    DIRECTORY_PATH_WINDOWS + connSingleton.getCurrZosConnection().getHost() + "\\" + dataset;
            pathToDirectoryWithFileName = pathToDirectory + "\\" + target;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            pathToDirectory = downloadPath.isBlank() ? downloadPath +
                    (!downloadPath.endsWith("/") ? "/" : "") +
                    connSingleton.getCurrZosConnection().getHost() + "/" + dataset :
                    DIRECTORY_PATH_MAC + connSingleton.getCurrZosConnection().getHost() + "/" + dataset;
            pathToDirectoryWithFileName = pathToDirectory + "/" + target;
        } else {
            throw new IllegalStateException(Constants.OS_ERROR);
        }
    }

    public String getPath() {
        LOG.debug("*** getPath ***");
        return pathToDirectory;
    }

    public String getPathWithFile() {
        LOG.debug("*** getPathWithFile ***");
        return pathToDirectoryWithFileName;
    }

    @Override
    public String toString() {
        return "PathService{" +
                "pathToDirectory='" + pathToDirectory + '\'' +
                ", pathToDirectoryWithFileName='" + pathToDirectoryWithFileName + '\'' +
                '}';
    }

}
