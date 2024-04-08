package zos.shell.service.dsn.edit;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.checksum.CheckSumService;
import zos.shell.service.dsn.download.Download;
import zos.shell.service.path.PathService;
import zos.shell.utility.DsnUtil;

import java.io.IOException;

public class Edit {

    private static final Logger LOG = LoggerFactory.getLogger(Edit.class);

    private final Download download;
    private final PathService pathService;
    private final CheckSumService checkSumService;
    private final Runtime rs = Runtime.getRuntime();

    public Edit(final Download download, final PathService pathService, final CheckSumService checkSumService) {
        LOG.debug("*** Edit ***");
        this.download = download;
        this.pathService = pathService;
        this.checkSumService = checkSumService;
    }

    public ResponseStatus open(final String dataset, final String target) {
        LOG.debug("*** open ***");
        ResponseStatus result;
        var datasetMember = DatasetMember.getDatasetAndMember(target);

        if (DsnUtil.isMember(target)) {
            // member input specified from current dataset
            result = download.member(dataset, target);
            this.pathService.createPathsForMember(dataset, target);
        } else if (datasetMember != null) {
            // dataset(member) input specified
            result = download.member(datasetMember.getDataset(), datasetMember.getMember());
            this.pathService.createPathsForMember(datasetMember.getDataset(), datasetMember.getMember());
        } else if (DsnUtil.isDataset(target)) {
            // sequential dataset input specified
            result = download.dataset(target);
            this.pathService.createPathsForSequentialDataset(target);
        } else {
            return new ResponseStatus(Constants.INVALID_PARAMETER, false);
        }

        try {
            if (result.isStatus()) {
                String pathFile;
                String editorName;
                pathFile = this.pathService.getPathWithFile();
                checkSumService.addCheckSum(pathFile);
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
