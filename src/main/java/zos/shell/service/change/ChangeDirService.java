package zos.shell.service.change;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.DsnUtil;
import zowe.client.sdk.zosfiles.dsn.input.ListParams;

public class ChangeDirService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeDirService.class);

    private final ListParams params = new ListParams.Builder().build();

    public ChangeDirService() {
        LOG.debug("*** ChangeDirService ***");
    }

    public ResponseStatus cd(String currDataSet, final String target) {
        LOG.debug("*** cd ***");
        if (DsnUtil.isMember(target) || DsnUtil.isDataset(target)) {
            return new ResponseStatus("success", true, target);
        } else if (target.equals("..") && !currDataSet.isBlank()) {
            var tokens = currDataSet.split("\\.");
            int length = tokens.length - 1;

            var str = new StringBuilder();
            for (var i = 0; i < length; i++) {
                str.append(tokens[i]);
                str.append(".");
            }

            var dataset = str.toString();
            if (dataset.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false, currDataSet);
            }
            dataset = dataset.substring(0, str.length() - 1);
            return new ResponseStatus("success", true, dataset);
        } else if (target.startsWith(".") && !currDataSet.isBlank()) {
            var newDataset = currDataSet + target;
            if (DsnUtil.isDataset(newDataset)) {
                return new ResponseStatus("success", true, newDataset);
            } else {
                return new ResponseStatus(Constants.INVALID_DATASET, false, currDataSet);
            }
        } else {
            return new ResponseStatus(Constants.INVALID_DATASET, false, currDataSet);
        }
    }

}
