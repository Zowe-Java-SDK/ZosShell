package com.command;

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
    private ZOSConnection connection;

    public Listing(ZOSConnection connection, TextTerminal<?> terminal) {
        this.connection = connection;
        this.terminal = terminal;
    }

    public List<String> lsl(String dataSet, boolean verbose) {
        try {
            dataSets = getDataSets(dataSet);
            members = getMembers(dataSet);
        } catch (Exception ignored) {
        }

        int membersSize = members.size();
        displayListStatus(membersSize, dataSets.size());
        displayDataSets(dataSets, dataSet);

        if (!verbose) {
            displayMembers(members);
            return members;
        }

        if (membersSize == 0)
            return members;
        int numOfColumns = 0;

        if (membersSize > 0 && membersSize < 100)
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
        String[] lines = new String[numOfLines + 1];

        int lineIndex = 0;
        for (int i = 0; i < membersSize; ) {
            int count = 1;
            String line = "";
            while (count % (numOfColumns + 1) != 0) {
                if (i >= membersSize) break;
                line += String.format("%-8s", members.get(i++)) + " ";
                count++;
            }
            lines[lineIndex++] = line;
        }

        Arrays.stream(lines).forEach(line -> {
            if (line != null) terminal.printf(line + "\n");
        });

        return members;
    }

    private List<Dataset> getDataSets(String dataSet) throws Exception {
        final var zosDsnList = new ZosDsnList(connection);
        final var params = new ListParams.Builder().build();
        return zosDsnList.listDsn(dataSet, params);
    }

    private List<String> getMembers(String dataSet) throws Exception {
        final var zosDsnList = new ZosDsnList(connection);
        final var params = new ListParams.Builder().build();
        return zosDsnList.listDsnMembers(dataSet, params);
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
            if (!ds.getDsname().get().equalsIgnoreCase(ignoreDataSet))
                terminal.printf(ds.getDsname().get() + "\n");
        });
    }

    private void displayMembers(List<String> members) {
        members.forEach(terminal::println);
    }

}
