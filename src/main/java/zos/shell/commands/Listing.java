package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import zos.shell.Constants;
import zos.shell.future.FutureDsnMembers;
import zos.shell.future.FutureListDsn;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;
import zowe.client.sdk.zosfiles.response.Dataset;
import zowe.client.sdk.zosfiles.types.AttributeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Listing {

    private final TextTerminal<?> terminal;
    private final long timeout;
    private List<String> members = new ArrayList<>();
    private List<Dataset> dataSets = new ArrayList<>();
    private final ZosDsnList zosDsnList;
    private ListParams params;

    public Listing(TextTerminal<?> terminal, ZosDsnList zosDsnList, long timeout) {
        this.terminal = terminal;
        this.zosDsnList = zosDsnList;
        this.timeout = timeout;
    }

    public void ls(String memberValue, String dataSet, boolean isColumnView)
            throws ExecutionException, InterruptedException, TimeoutException {
        ListParams.Builder paramsBuilder =  new ListParams.Builder()
                .maxLength("0")  // return all
                .responseTimeout(String.valueOf(timeout));
        if (!isColumnView) { // ls -1
            params = paramsBuilder.attribute(AttributeType.BASE).build();
        } else { // ls
            params = paramsBuilder.build();
        }
        var member = Optional.ofNullable(memberValue);
        if (member.isPresent()) {
            member = Optional.of(memberValue.toUpperCase());
        }

        try {
            dataSets = getDataSets(dataSet);
            members = getMembers(dataSet);
        } catch (Exception ignore) {
        }

        member.ifPresentOrElse((m) -> {
            final var index = m.indexOf("*");
            final var searchForMember = index == -1 ? m : m.substring(0, index);
            if (m.equals(searchForMember)) {
                members = members.stream().filter(i -> i.equals(searchForMember)).collect(Collectors.toList());
            } else {
                members = members.stream().filter(i -> i.startsWith(searchForMember)).collect(Collectors.toList());
            }
        }, () -> displayDataSets(dataSets, dataSet, isColumnView));
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

        if (membersSize == 0) {
            return;
        }

        final var line = new StringBuilder();
        for (String item : members) {
            line.append(String.format("%-8s", item));
            line.append(" ");
        }
        terminal.println(line.toString());
    }

    private List<String> getMembers(String dataSet) throws ExecutionException, InterruptedException, TimeoutException {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureDsnMembers(zosDsnList, dataSet, params));
        return submit.get(timeout, TimeUnit.SECONDS);
    }

    private List<Dataset> getDataSets(String dataSet) throws ExecutionException, InterruptedException, TimeoutException {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureListDsn(zosDsnList, dataSet, params));
        return submit.get(timeout, TimeUnit.SECONDS);
    }

    private void displayListStatus(int membersSize, int dataSetsSize) {
        if (membersSize == 0 && dataSetsSize == 1) {
            terminal.println(Constants.NO_MEMBERS);
        }
        if (membersSize == 0 && dataSetsSize == 0) {
            terminal.println(Constants.NO_LISTING);
        }
    }

    private void displayDataSets(List<Dataset> dataSets, String ignoreCurrDataSet, boolean isColumnView) {
        if (dataSets.isEmpty()) {
            return;
        }
        if (!isColumnView) { // ls -l
            final var columnFormat = "%-11s %-11s %-8s %-5s %-5s %-6s %-7s %-5s";
            terminal.println(String.format(columnFormat, "cdate", "rdate", "vol", "dsorg", "recfm", "blksz", "dsntp", "dsname")
            );
            dataSets.forEach(ds -> {
                final var dsname = ds.getDsname().orElse("");
                if (!dsname.equalsIgnoreCase(ignoreCurrDataSet)) {
                    terminal.println(String.format(columnFormat, ds.getCdate().orElse(""), ds.getRdate().orElse(""),
                            ds.getVol().orElse(""), ds.getDsorg().orElse(""), ds.getRecfm().orElse(""),
                            ds.getBlksz().orElse(""), ds.getDsntp().orElse(""), dsname));
                }
            });
        } else { // ls
            dataSets.forEach(ds -> {
                final var dsname = ds.getDsname().orElse("");
                if (!dsname.equalsIgnoreCase(ignoreCurrDataSet)) {
                    terminal.println(dsname);
                }
            });
        }
    }

    private void displayMembers(List<String> members) {
        members.forEach(terminal::println);
    }

}