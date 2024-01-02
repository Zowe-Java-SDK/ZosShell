package zos.shell.service.dsn;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;

import java.io.IOException;

public class ViCmd {

    private static final Logger LOG = LoggerFactory.getLogger(ViCmd.class);

    private final DownloadCmd download;
    private final Runtime rs = Runtime.getRuntime();

    public ViCmd(DownloadCmd download) {
        LOG.debug("*** Vi ***");
        this.download = download;
    }

    public ResponseStatus vi(String dataSet, String target) {
        LOG.debug("*** vi ***");
        ResponseStatus result;
        final var dataSetMember = Util.getDatasetAndMember(target);

        if (Util.isMember(target)) {
            // member input specified from current dataset
            result = download.download(dataSet, target);
        } else if (dataSetMember != null) {
            // dataset(member) input specified
            dataSet = dataSetMember.getDataSet();
            target = dataSetMember.getMember();
            result = download.download(dataSet, target);
        } else {
            // dataset input specified i.e. sequential dataset
            result = download.download(target);
            dataSet = Constants.SEQUENTIAL_DIRECTORY_LOCATION;
        }

        final var successMsg = "opening " + target + " in editor...";
        final var errorMsg = "\ncannot open " + target + ", try again...";
        try {
            if (result.isStatus()) {
                String pathFile;
                String editorName;
                if (SystemUtils.IS_OS_WINDOWS) {
                    pathFile = DownloadCmd.DIRECTORY_PATH_WINDOWS + dataSet + "\\" + target;
                    editorName = Constants.WINDOWS_EDITOR_NAME;
                } else if (SystemUtils.IS_OS_MAC_OSX) {
                    pathFile = DownloadCmd.DIRECTORY_PATH_MAC + dataSet + "/" + target;
                    editorName = Constants.MAC_EDITOR_NAME;
                } else {
                    return new ResponseStatus(Constants.OS_ERROR, false);
                }
                rs.exec(editorName + " " + pathFile);
            } else {
                return new ResponseStatus(Util.getMsgAfterArrow(result.getMessage()) + errorMsg, false);
            }
        } catch (IOException e) {
            return new ResponseStatus(Util.getMsgAfterArrow(e.getMessage()) + errorMsg, false);
        }
        return new ResponseStatus(successMsg, true);
    }

}
