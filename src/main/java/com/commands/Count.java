package com.commands;

import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;
import zowe.client.sdk.zosfiles.response.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Count {

    private final TextTerminal<?> terminal;
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Count(TextTerminal<?> terminal, ZosDsnList zosDsnList) {
        this.terminal = terminal;
        this.zosDsnList = zosDsnList;
    }

    public void count(String dataSet, String param) {
        AtomicInteger dataSetCount = new AtomicInteger();
        List<Dataset> ds = new ArrayList<>();
        List<String> members = new ArrayList<>();
        try {
            if ("members".equalsIgnoreCase(param)) {
                members = zosDsnList.listDsnMembers(dataSet, params);
            }
            if ("datasets".equalsIgnoreCase(param)) {
                ds = zosDsnList.listDsn(dataSet, params);
            }
            ds.forEach(item -> {
                if (!item.getDsname().orElse("n\\a").equalsIgnoreCase(dataSet)) {
                    dataSetCount.getAndIncrement();
                }
            });
        } catch (Exception e) {
            terminal.println("0");
            return;
        }
        terminal.println(String.valueOf(members.size() + dataSetCount.get()));
    }

}
