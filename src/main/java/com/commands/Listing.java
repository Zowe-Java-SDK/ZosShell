package com.commands;

import com.Constants;
import com.future.FutureDsnMembers;
import com.future.FutureListDsn;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;
import zowe.client.sdk.zosfiles.response.Dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Listing {

    private final TextTerminal<?> terminal;
    private final long timeOutValue;
    private List<String> members = new ArrayList<>();
    private List<Dataset> dataSets = new ArrayList<>();
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();
    private final SwingTextTerminal mainTerminal;

    public Listing(TextTerminal<?> terminal, ZosDsnList zosDsnList, SwingTextTerminal mainTerminal, long timeOutValue) {
        this.terminal = terminal;
        this.zosDsnList = zosDsnList;
        this.mainTerminal = mainTerminal;
        this.timeOutValue = timeOutValue;
    }

    public void ls(String memberValue, String dataSet, boolean isColumnView) throws ExecutionException, InterruptedException, TimeoutException {
        Optional<String> member = Optional.ofNullable(memberValue);
        if (member.isPresent()) {
            member = Optional.of(memberValue.toUpperCase());
        }

        dataSets = getDataSets(dataSet);
        members = getMembers(dataSet);

        member.ifPresentOrElse((m) -> {
            final var index = m.indexOf("*");
            final var searchForMember = index == -1 ? m : m.substring(0, index);
            if (m.equals(searchForMember)) {
                members = members.stream().filter(i -> i.equals(searchForMember)).collect(Collectors.toList());
            } else {
                members = members.stream().filter(i -> i.startsWith(searchForMember)).collect(Collectors.toList());
            }
        }, () -> displayDataSets(dataSets, dataSet));
        final var membersSize = members.size();
        if (member.isPresent() && membersSize == 0) {
            terminal.println(Constants.NO_MEMBERS);
            return;
        }
        displayListStatus(membersSize, dataSets.size());

        if (!isColumnView) {
            displayMembers(members);
            return;
        }

        final var screenWidth = mainTerminal.getTextPane().getWidth();

        if (membersSize == 0) {
            return;
        }
        var numOfColumns = 3;

        if (screenWidth > 350 && screenWidth < 450) {
            numOfColumns = 4;
        } else if (screenWidth > 450 && screenWidth < 550) {
            numOfColumns = 5;
        } else if (screenWidth > 550 && screenWidth < 650) {
            numOfColumns = 6;
        } else if (screenWidth > 650 && screenWidth < 750) {
            numOfColumns = 7;
        } else if (screenWidth > 750 && screenWidth < 850) {
            numOfColumns = 8;
        } else if (screenWidth > 850 && screenWidth < 950) {
            numOfColumns = 9;
        } else if (screenWidth > 950 && screenWidth < 1050) {
            numOfColumns = 10;
        } else if (screenWidth > 1050 && screenWidth < 1150) {
            numOfColumns = 12;
        } else if (screenWidth > 1200 && screenWidth < 1500) {
            numOfColumns = 14;
        } else if (screenWidth > 1500 && screenWidth < 2000) {
            numOfColumns = 16;
        } else if (screenWidth > 2000 && screenWidth < 2500) {
            numOfColumns = 20;
        } else if (screenWidth > 2500) {
            numOfColumns = 22;
        }

        var numOfLines = membersSize / numOfColumns;
        var lines = new String[numOfLines + 1];

        int lineIndex = 0;
        for (var i = 0; i < membersSize; ) {
            int count = 1;
            StringBuilder line = new StringBuilder();
            while (count % (numOfColumns + 1) != 0) {
                if (i >= membersSize) {
                    break;
                }
                line.append(String.format("%-8s", members.get(i++)));
                line.append(" ");
                count++;
            }
            lines[lineIndex++] = line.toString();
        }

        Arrays.stream(lines).forEach(line -> {
            if (line != null) {
                terminal.println(line);
            }
        });
    }

    private List<String> getMembers(String dataSet) throws ExecutionException, InterruptedException, TimeoutException {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureDsnMembers(zosDsnList, dataSet, params));
        return submit.get(timeOutValue, TimeUnit.SECONDS);
    }

    private List<Dataset> getDataSets(String dataSet) throws ExecutionException, InterruptedException, TimeoutException {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureListDsn(zosDsnList, dataSet, params));
        return submit.get(timeOutValue, TimeUnit.SECONDS);
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
            var dsName = ds.getDsname().orElse("");
            if (!dsName.equalsIgnoreCase(ignoreDataSet)) {
                terminal.println(dsName);
            }
        });
    }

    private void displayMembers(List<String> members) {
        members.forEach(terminal::println);
    }

}
