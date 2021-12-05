package com.commands;

import com.Constants;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsnList;
import zosfiles.input.ListParams;
import zosfiles.response.Dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Listing {

    private final TextTerminal<?> terminal;
    private List<String> members = new ArrayList<>();
    private List<Dataset> dataSets = new ArrayList<>();
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Listing(ZOSConnection connection, TextTerminal<?> terminal) {
        this.terminal = terminal;
        this.zosDsnList = new ZosDsnList(connection);
    }

    public List<String> ls(String dataSet, boolean verbose) {
        try {
            dataSets = getDataSets(dataSet);
            members = getMembers(dataSet);
        } catch (Exception ignored) {
        }

        final var membersSize = members.size();
        displayListStatus(membersSize, dataSets.size());
        displayDataSets(dataSets, dataSet);

        if (!verbose) {
            displayMembers(members);
            return members;
        }

        if (membersSize == 0)
            return members;
        int numOfColumns = 0;

        if (membersSize < 100)
            numOfColumns = 3;
        else if (membersSize > 100 && membersSize < 300) {
            numOfColumns = 4;
        } else if (membersSize > 300 && membersSize < 500) {
            numOfColumns = 5;
        } else if (membersSize > 500 && membersSize < 700) {
            numOfColumns = 6;
        } else if (membersSize > 700 && membersSize < 900) {
            numOfColumns = 7;
        } else if (membersSize >= 1000)
            numOfColumns = 8;

        var numOfLines = membersSize / numOfColumns;
        var lines = new String[numOfLines + 1];

        int lineIndex = 0;
        for (int i = 0; i < membersSize; ) {
            int count = 1;
            StringBuilder line = new StringBuilder();
            while (count % (numOfColumns + 1) != 0) {
                if (i >= membersSize) break;
                line.append(String.format("%-8s", members.get(i++)));
                line.append(" ");
                count++;
            }
            lines[lineIndex++] = line.toString();
        }

        Arrays.stream(lines).forEach(line -> {
            if (line != null) terminal.println(line);
        });

        return members;
    }

    public List<String> getMembers(String dataSet) throws Exception {
        return zosDsnList.listDsnMembers(dataSet, params);
    }

    private List<Dataset> getDataSets(String dataSet) throws Exception {
        return zosDsnList.listDsn(dataSet, params);
    }

    private void displayListStatus(int membersSize, int dataSetsSize) {
        if (membersSize == 0 && dataSetsSize == 1) {
            terminal.println(Constants.NO_MEMBERS);
        }
        if (membersSize == 0 && dataSetsSize == 0) {
            terminal.println(Constants.NO_LISTING);
        }
    }

    private void displayDataSets(List<Dataset> dataSets, String ignoreDataSet) {
        dataSets.forEach(ds -> {
            String dsName = ds.getDsname().orElse("");
            if (!dsName.equalsIgnoreCase(ignoreDataSet))
                terminal.println(dsName);
        });
    }

    private void displayMembers(List<String> members) {
        members.forEach(terminal::println);
    }

}
