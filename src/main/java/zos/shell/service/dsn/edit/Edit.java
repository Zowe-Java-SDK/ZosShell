package zos.shell.service.dsn.edit;

import com.google.common.base.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.DownloadCmd;
import zos.shell.utility.Util;

import java.io.IOException;

public class Edit {

    private static final Logger LOG = LoggerFactory.getLogger(Edit.class);

    private final DownloadCmd download;
    private final Runtime rs = Runtime.getRuntime();

    public Edit(final DownloadCmd download) {
        LOG.debug("*** Edit ***");
        this.download = download;
    }

    public ResponseStatus open(String dataset, String target) {
        LOG.debug("*** open ***");
        ResponseStatus result;
        final var dataSetMember = Util.getDatasetAndMember(target);

        if (Util.isMember(target)) {
            // member input specified from current dataset
            result = download.download(dataset, target);
        } else if (dataSetMember != null) {
            // dataset(member) input specified
            dataset = dataSetMember.getDataSet();
            target = dataSetMember.getMember();
            result = download.download(dataset, target);
        } else {
            // target input specified i.e. sequential dataset
            result = download.download(target);
            dataset = Constants.SEQUENTIAL_DIRECTORY_LOCATION;
        }

        final var arrowMsg = Strings.padStart(target,
                Constants.STRING_PAD_LENGTH, ' ') + Constants.ARROW;

        try {
            if (result.isStatus()) {
                String pathFile;
                String editorName;
                if (SystemUtils.IS_OS_WINDOWS) {
                    pathFile = DownloadCmd.DIRECTORY_PATH_WINDOWS + dataset + "\\" + target;
                    editorName = Constants.WINDOWS_EDITOR_NAME;
                } else if (SystemUtils.IS_OS_MAC_OSX) {
                    pathFile = DownloadCmd.DIRECTORY_PATH_MAC + dataset + "/" + target;
                    editorName = Constants.MAC_EDITOR_NAME;
                } else {
                    return new ResponseStatus(Constants.OS_ERROR, false);
                }
                rs.exec(editorName + " " + pathFile);
            } else {
                return new ResponseStatus(arrowMsg + result.getMessage(), false);
            }
        } catch (IOException e) {
            return new ResponseStatus(arrowMsg + result.getMessage(), false);
        }

        return new ResponseStatus(arrowMsg + "opened in editor", true);
    }

}
