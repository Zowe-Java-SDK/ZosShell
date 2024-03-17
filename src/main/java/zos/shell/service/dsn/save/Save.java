package zos.shell.service.dsn.save;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.checksum.CheckSumService;
import zos.shell.service.path.PathService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Save {

    private static final Logger LOG = LoggerFactory.getLogger(Save.class);

    private final DsnWrite dsnWrite;
    private final PathService pathService;
    private final CheckSumService checkSumService;

    public Save(final DsnWrite dsnWrite, final PathService pathService, final CheckSumService checkSumService) {
        LOG.debug("*** Save ***");
        this.dsnWrite = dsnWrite;
        this.pathService = pathService;
        this.checkSumService = checkSumService;
    }

    public ResponseStatus save(final String dataset, final String target) {
        LOG.debug("*** save ***");
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC_OSX) {
            return new ResponseStatus(Constants.OS_ERROR, false);
        }

        var datasetMember = DatasetMember.getDatasetAndMember(target);
        boolean isSequentialDataset = false;
        PathService pathService;

        if (DsnUtil.isMember(target)) {
            // member input specified from current dataset
            this.pathService.createPathsForMember(dataset, target);
        } else if (datasetMember != null) {
            // dataset(member) input specified
            this.pathService.createPathsForMember(datasetMember.getDataset(), datasetMember.getMember());
        } else if (DsnUtil.isDataset(target)) {
            //  sequential dataset input specified
            this.pathService.createPathsForSequentialDataset(target);
            isSequentialDataset = true;
        } else {
            return new ResponseStatus(Constants.INVALID_PARAMETER, false);
        }

        try (var br = new BufferedReader(new FileReader(this.pathService.getPathWithFile()))) {
            var sb = new StringBuilder();
            var line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            var content = sb.toString().replaceAll("(\\r)", "");

            String checksum = checkSumService.calculateCheckSum(this.pathService.getPathWithFile());
            if (checksum.equals(checkSumService.getCheckSum(this.pathService.getPathWithFile()))) {
                return new ResponseStatus("nothing to save, perform editor save and try again...", false);
            }
            checkSumService.addCheckSum(this.pathService.getPathWithFile());

            if (isSequentialDataset) {
                dsnWrite.write(target, content);
            } else if (datasetMember != null) {
                dsnWrite.write(datasetMember.getDataset(), datasetMember.getMember(), content);
            } else {
                dsnWrite.write(dataset, target, content);
            }
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        } catch (IOException e) {
            return new ResponseStatus(e.getMessage(), false);
        }

        return new ResponseStatus(target + " saved", true);
    }

}
