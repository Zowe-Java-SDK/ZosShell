package com.commands;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsnList;
import zosfiles.input.ListParams;
import zosfiles.response.Dataset;

import java.util.List;

public class ChangeDir {

    private final TextTerminal<?> terminal;
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public ChangeDir(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.zosDsnList = new ZosDsnList(connection);
    }

    public String cd(String currDataSet, String param) {
        if (Util.isDataSet(param)) {
            return param;
        } else if (param.equals("..") && !currDataSet.isEmpty()) {
            String[] tokens = currDataSet.split("\\.");
            final var length = tokens.length - 1;
            if (length == 1) {
                terminal.println(Constants.HIGH_QUALIFIER_ERROR);
                return currDataSet;
            }

            var str = new StringBuilder();
            for (int i = 0; i < length; i++) {
                str.append(tokens[i]);
                str.append(".");
            }

            String dataSet = str.toString();
            dataSet = dataSet.substring(0, str.length() - 1);
            return dataSet;
        } else {
            List<Dataset> dsLst;
            try {
                dsLst = zosDsnList.listDsn(currDataSet, params);
            } catch (Exception e) {
                if (e.getMessage().contains("Connection refused")) {
                    terminal.println(Constants.SEVERE_ERROR);
                    return currDataSet;
                }
                Util.printError(terminal, e.getMessage());
                return currDataSet;
            }
            var findDataSet = currDataSet + "." + param;
            boolean found = dsLst.stream().anyMatch(d -> d.getDsname().get().contains(findDataSet));
            if (found)
                currDataSet += "." + param;
            else terminal.println(Constants.DATASET_OR_HIGH_QUALIFIER_ERROR);
            return currDataSet;
        }
    }

}
