package com.command;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsn;
import zosfiles.ZosDsnList;
import zosfiles.input.ListParams;

import java.util.ArrayList;
import java.util.List;

public class Delete {

    private final TextTerminal<?> terminal;
    private final ZOSConnection connection;

    public Delete(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.connection = connection;
    }

    public void rm(String currDataSet, String param) {
        try {
            final var zosDsn = new ZosDsn(connection);
            final var zosDsnList = new ZosDsnList(connection);
            final var params = new ListParams.Builder().build();
            List<String> members = new ArrayList<>();

            if ("*".equals(param)) {
                if (currDataSet.isEmpty()) {
                    terminal.printf(Constants.DELETE_NOTHING_ERROR + "\n");
                    return;
                }
                try {
                    members = zosDsnList.listDsnMembers(currDataSet, params);
                } catch (Exception e) {
                    Util.printError(terminal, e.getMessage());
                }
                members.forEach(m -> {
                    try {
                        zosDsn.deleteDsn(currDataSet, m);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            if (Util.isMember(param)) {
                if (currDataSet.isEmpty()) {
                    terminal.printf(Constants.DELETE_NOTHING_ERROR + "\n");
                    return;
                }
                try {
                    members = zosDsnList.listDsnMembers(currDataSet, params);
                    if (members.stream().noneMatch(param::equalsIgnoreCase)) {
                        terminal.printf(Constants.DELETE_NOTHING_ERROR + "\n");
                        return;
                    }
                    zosDsn.deleteDsn(currDataSet, param);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            if (param.contains("(") && param.contains(")")) {
                String member;
                String dataset;

                int index = param.indexOf("(");
                dataset = param.substring(0, index);
                if (!Util.isDataSet(dataset)) {
                    terminal.printf(Constants.DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR + "\n");
                    return;
                }

                member = param.substring(index + 1, param.length() - 1);
                try {
                    zosDsn.deleteDsn(dataset, member);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Util.isDataSet(param)) {
                zosDsn.deleteDsn(param);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.printf(Constants.SEVERE_ERROR + "\n");
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
    }

}
