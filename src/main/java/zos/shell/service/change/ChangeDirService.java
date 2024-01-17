package zos.shell.service.change;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;

import java.util.List;

public class ChangeDirService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeDirService.class);

    private final DsnList dsnList;
    private final ListParams params = new ListParams.Builder().build();

    public ChangeDirService(final DsnList dsnList) {
        LOG.debug("*** ChangeDirectoryService ***");
        this.dsnList = dsnList;
    }

    public ResponseStatus cd(String currDataSet, final String target) {
        LOG.debug("*** cd ***");
        if (DsnUtil.isDataSet(target)) {
            return new ResponseStatus("success", true, target);
        } else if (target.equals("..") && !currDataSet.isBlank()) {
            var tokens = currDataSet.split("\\.");
            final var length = tokens.length - 1;
            if (length == 1) {
                return new ResponseStatus(Constants.HIGH_QUALIFIER_ERROR, false, currDataSet);
            }

            var str = new StringBuilder();
            for (var i = 0; i < length; i++) {
                str.append(tokens[i]);
                str.append(".");
            }

            var dataset = str.toString();
            dataset = dataset.substring(0, str.length() - 1);
            return new ResponseStatus("success", true, dataset);
        } else {
            List<Dataset> dsLst;
            try {
                dsLst = dsnList.getDatasets(currDataSet, params);
            } catch (ZosmfRequestException e) {
                final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
                return new ResponseStatus(errMsg != null ? errMsg : e.getMessage(), false, currDataSet);
            } catch (IllegalArgumentException e) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false, currDataSet);
            }
            var findDataSet = currDataSet + "." + target;
            var found = dsLst.stream().anyMatch(d -> d.getDsname().orElse("").contains(findDataSet));
            if (found) {
                currDataSet += "." + target;
            } else {
                return new ResponseStatus(Constants.DATASET_OR_HIGH_QUALIFIER_ERROR, false, currDataSet);
            }
            return new ResponseStatus("success", true, currDataSet);
        }
    }

}
