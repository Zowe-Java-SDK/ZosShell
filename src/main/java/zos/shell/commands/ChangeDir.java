package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;

import java.util.List;

public class ChangeDir {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeDir.class);

    private final TextTerminal<?> terminal;
    private final DsnList dsnList;
    private final ListParams params = new ListParams.Builder().build();

    public ChangeDir(TextTerminal<?> terminal, DsnList dsnList) {
        LOG.debug("*** ChangeDir ***");
        this.terminal = terminal;
        this.dsnList = dsnList;
    }

    public String cd(String currDataSet, String param) {
        LOG.debug("*** cd ***");
        if (Util.isDataSet(param)) {
            return param;
        } else if (param.equals("..") && !currDataSet.isBlank()) {
            var tokens = currDataSet.split("\\.");
            final var length = tokens.length - 1;
            if (length == 1) {
                terminal.println(Constants.HIGH_QUALIFIER_ERROR);
                return currDataSet;
            }

            var str = new StringBuilder();
            for (var i = 0; i < length; i++) {
                str.append(tokens[i]);
                str.append(".");
            }

            var dataSet = str.toString();
            dataSet = dataSet.substring(0, str.length() - 1);
            return dataSet;
        } else {
            List<Dataset> dsLst;
            try {
                dsLst = dsnList.getDatasets(currDataSet, params);
            } catch (ZosmfRequestException e) {
                final String errMsg = Util.getResponsePhrase(e.getResponse());
                terminal.println((errMsg != null ? errMsg : e.getMessage()));
                return currDataSet;
            } catch (IllegalArgumentException e) {
                terminal.println(Constants.DATASET_NOT_SPECIFIED);
                return currDataSet;
            }
            var findDataSet = currDataSet + "." + param;
            var found = dsLst.stream().anyMatch(d -> d.getDsname().orElse("").contains(findDataSet));
            if (found) {
                currDataSet += "." + param;
            } else {
                terminal.println(Constants.DATASET_OR_HIGH_QUALIFIER_ERROR);
            }
            return currDataSet;
        }
    }

}
