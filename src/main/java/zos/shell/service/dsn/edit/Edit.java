package zos.shell.service.dsn.edit;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DataSetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.Download;
import zos.shell.service.dsn.download.DownloadDsnCmd;
import zos.shell.utility.DsnUtil;

import java.io.IOException;

public class Edit {

    private static final Logger LOG = LoggerFactory.getLogger(Edit.class);

    private final Download download;
    private final Runtime rs = Runtime.getRuntime();

    public Edit(final Download download) {
        LOG.debug("*** Edit ***");
        this.download = download;
    }

    public ResponseStatus open(String dataset, String target) {
        LOG.debug("*** open ***");
        ResponseStatus result;
        final var dataSetMember = DataSetMember.getDatasetAndMember(target);

        if (DsnUtil.isMember(target)) {
            // member input specified from current dataset
            result = download.member(dataset, target);
        } else if (dataSetMember != null) {
            // dataset(member) input specified
            dataset = dataSetMember.getDataSet();
            target = dataSetMember.getMember();
            result = download.member(dataset, target);
        } else {
            // target input specified must be sequential dataset
            result = download.dataset(target);
            dataset = Constants.SEQUENTIAL_DIRECTORY_LOCATION;
        }

        try {
            if (result.isStatus()) {
                String pathFile;
                String editorName;
                if (SystemUtils.IS_OS_WINDOWS) {
                    pathFile = DownloadDsnCmd.DIRECTORY_PATH_WINDOWS + dataset + "\\" + target;
                    editorName = Constants.WINDOWS_EDITOR_NAME;
                } else if (SystemUtils.IS_OS_MAC_OSX) {
                    pathFile = DownloadDsnCmd.DIRECTORY_PATH_MAC + dataset + "/" + target;
                    editorName = Constants.MAC_EDITOR_NAME;
                } else {
                    return new ResponseStatus(Constants.OS_ERROR, false);
                }
                rs.exec(editorName + " " + pathFile);
            } else {
                return new ResponseStatus(result.getMessage(), false);
            }
        } catch (IOException e) {
            return new ResponseStatus(result.getMessage(), false);
        }

        return new ResponseStatus("opened in editor", true);
    }

}
