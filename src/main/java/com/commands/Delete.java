package com.commands;

import com.Constants;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import rest.Response;
import zosfiles.ZosDsn;
import zosfiles.ZosDsnList;
import zosfiles.input.ListParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Delete {

    private final TextTerminal<?> terminal;
    private final ZosDsn zosDsn;
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Delete(TextTerminal<?> terminal, ZosDsn zosDsn, ZosDsnList zosDsnList) {
        this.terminal = terminal;
        this.zosDsn = zosDsn;
        this.zosDsnList = zosDsnList;
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
                var success = new AtomicBoolean(true);
                members.forEach(m -> {
                    try {
                        Response response = zosDsn.deleteDsn(currDataSet, m);
                        if (failed(response)) {
                            success.set(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                if (success.get())
                    terminal.println("delete succeeded...");
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
                    Response response = zosDsn.deleteDsn(currDataSet, param);
                    if (failed(response)) return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                terminal.println(param + " successfully deleted...");
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
                    var response = zosDsn.deleteDsn(dataset, member);
                    if (failed(response)) return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                terminal.println(param + " successfully deleted...");
                return;
            }

            if (Util.isDataSet(param)) {
                var response = zosDsn.deleteDsn(param);
                if (failed(response)) return;
            }
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
            return;
        }
        terminal.println(param + " successfully deleted...");
    }

    private boolean failed(Response response) {
        var code = response.getStatusCode().orElse(-1);
        if (Util.isHttpError(code)) {
            terminal.println("delete operation failed with http code + " + code + ", try again...");
            return true;
        }
        return false;
    }

}
