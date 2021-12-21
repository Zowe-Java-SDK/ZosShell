package com.commands;

import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsnList;
import zosfiles.input.ListParams;
import zosfiles.response.Dataset;

import java.util.ArrayList;
import java.util.List;

public class Count {

    private final TextTerminal<?> terminal;
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Count(TextTerminal<?> terminal, ZosDsnList zosDsnList) {
        this.terminal = terminal;
        this.zosDsnList = zosDsnList;
    }

    public void count(String dataSet, String param) {
        List<Dataset> ds = new ArrayList<>();
        List<String> members = new ArrayList<>();
        try {
            if ("members".equalsIgnoreCase(param)) {
                members = zosDsnList.listDsnMembers(dataSet, params);
            }
            if ("datasets".equalsIgnoreCase(param)) {
                ds = zosDsnList.listDsn(dataSet, params);
            }
        } catch (Exception e) {
            terminal.println("0");
            return;
        }
        terminal.println(String.valueOf(members.size() + (ds.size() >= 1 ? ds.size() - 1 : ds.size())));
    }

}
