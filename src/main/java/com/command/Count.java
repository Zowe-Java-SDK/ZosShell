package com.command;

import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsnList;
import zosfiles.input.ListParams;
import zosfiles.response.Dataset;

import java.util.ArrayList;
import java.util.List;

public class Count {

    private final TextTerminal<?> terminal;
    private final ZOSConnection connection;

    public Count(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.connection = connection;
    }

    public void count(String dataSet, String param) {
        List<Dataset> ds = new ArrayList<>();
        List<String> members = new ArrayList<>();
        try {
            final var zosDsnList = new ZosDsnList(connection);
            final var params = new ListParams.Builder().build();
            if ("members".equalsIgnoreCase(param)) {
                members = zosDsnList.listDsnMembers(dataSet, params);
            }
            if ("datasets".equalsIgnoreCase(param)) {
                ds = zosDsnList.listDsn(dataSet, params);
            }
        } catch (Exception e) {
            terminal.printf("0" + "\n");
            return;
        }
        terminal.printf(members.size() + ds.size() + "\n");
    }

}
