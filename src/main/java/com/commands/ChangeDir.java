package com.commands;

import com.Constants;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsnList;
import zosfiles.input.ListParams;
import zosfiles.response.Dataset;

import java.util.List;

public class ChangeDir {

    private final TextTerminal<?> terminal;
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public ChangeDir(TextTerminal<?> terminal, ZosDsnList zosDsnList) {
        this.terminal = terminal;
        this.zosDsnList = zosDsnList;
    }

    public String cd(String currDataSet, String param) {
        if (Util.isDataSet(param)) {
            return param;
        } else if (param.equals("..") && !currDataSet.isEmpty()) {
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
                dsLst = zosDsnList.listDsn(currDataSet, params);
            } catch (Exception e) {
                if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                    terminal.println(Constants.SEVERE_ERROR);
                    return currDataSet;
                }
                Util.printError(terminal, e.getMessage());
                return currDataSet;
            }
            var findDataSet = currDataSet + "." + param;
            var found = dsLst.stream().anyMatch(d -> d.getDsname().orElse("").contains(findDataSet));
            if (found)
                currDataSet += "." + param;
            else terminal.println(Constants.DATASET_OR_HIGH_QUALIFIER_ERROR);
            return currDataSet;
        }
    }

}
