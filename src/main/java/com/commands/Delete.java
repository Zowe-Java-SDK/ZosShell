package com.commands;

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
    private final ZosDsn zosDsn;
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Delete(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.zosDsn = new ZosDsn(connection);
        this.zosDsnList = new ZosDsnList(connection);
    }

    public void rm(String currDataSet, String param) {
        try {
            List<String> members = new ArrayList<>();

            if ("*".equals(param)) {
                if (currDataSet.isEmpty()) {
                    terminal.println(Constants.DELETE_NOTHING_ERROR);
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
                    terminal.println(Constants.DELETE_NOTHING_ERROR);
                    return;
                }
                try {
                    members = zosDsnList.listDsnMembers(currDataSet, params);
                    if (members.stream().noneMatch(param::equalsIgnoreCase)) {
                        terminal.println(Constants.DELETE_NOTHING_ERROR);
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

                var index = param.indexOf("(");
                dataset = param.substring(0, index);
                if (!Util.isDataSet(dataset)) {
                    terminal.println(Constants.DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR);
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
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
    }

}
