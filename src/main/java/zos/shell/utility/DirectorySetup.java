package zos.shell.utility;

import zos.shell.Constants;
import org.apache.commons.lang3.SystemUtils;

public class DirectorySetup {

    private static final String DIRECTORY_PATH_WINDOWS = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\";
    private static final String DIRECTORY_PATH_MAC = Constants.PATH_FILE_DIRECTORY_MAC + "/";
    private String directoryPath;
    private String fileNamePath;

    public void initialize(String directoryName, String fileName) throws Exception {
        if (SystemUtils.IS_OS_WINDOWS) {
            directoryPath = DIRECTORY_PATH_WINDOWS + directoryName;
            fileNamePath = directoryPath + "\\" + fileName;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            directoryPath = DIRECTORY_PATH_MAC + directoryName;
            fileNamePath = directoryPath + "/" + fileName;
        } else {
            throw new Exception(Constants.OS_ERROR);
        }
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public String getFileNamePath() {
        return fileNamePath;
    }

    @Override
    public String toString() {
        return "DirectorySetup{" +
                "directoryPath='" + directoryPath + '\'' +
                ", fileNamePath='" + fileNamePath + '\'' +
                '}';
    }

}
