package zos.shell.service.dsn.edit;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.Download;
import zos.shell.service.path.PathService;
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
        var datasetMember = DatasetMember.getDatasetAndMember(target);

        PathService pathService;
        if (DsnUtil.isMember(target)) {
            // member input specified from current dataset
            result = download.member(dataset, target);
            pathService = new PathService(dataset, target);
        } else if (datasetMember != null) {
            // dataset(member) input specified
            dataset = datasetMember.getDataset();
            target = datasetMember.getMember();
            result = download.member(dataset, target);
            pathService = new PathService(dataset, target);
        } else {
            // target input specified must be sequential dataset
            result = download.dataset(target);
            pathService = new PathService(target);
        }

        try {
            if (result.isStatus()) {
                String pathFile;
                String editorName;
                pathFile = pathService.getPathWithFile();
                if (SystemUtils.IS_OS_WINDOWS) {
                    editorName = Constants.WINDOWS_EDITOR_NAME;
                } else if (SystemUtils.IS_OS_MAC_OSX) {
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
