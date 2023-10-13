package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.future.FutureDsnMembers;
import zos.shell.future.FutureListDsn;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;
import zowe.client.sdk.zosfiles.response.Dataset;
import zowe.client.sdk.zosfiles.response.Member;
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

    private static final Logger LOG = LoggerFactory.getLogger(Listing.class);

    private final TextTerminal<?> terminal;
    private final long timeout;
    private List<Member> members = new ArrayList<>();
    private List<Dataset> dataSets = new ArrayList<>();
    private final ZosDsnList zosDsnList;
    private ListParams params;
    private boolean isDataSets = false;

    public Listing(TextTerminal<?> terminal, ZosDsnList zosDsnList, long timeout) {
        LOG.debug("*** Listing ***");
        this.terminal = terminal;
        this.zosDsnList = zosDsnList;
        this.timeout = timeout;
    }

    public void ls(String memberValue, String dataSet, boolean isColumnView, boolean isAttributes)
            throws TimeoutException {
        LOG.debug("*** ls ***");
        final var paramsBuilder = new ListParams.Builder()
                .maxLength("0")  // return all
                .responseTimeout(String.valueOf(timeout));
        if (!isColumnView && isAttributes) { // ls -1
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
            isDataSets = dataSets.size() > 1;
            members = getMembers(dataSet);
        } catch (TimeoutException e) {
            throw new TimeoutException(e.getMessage());
        } catch (Exception ignored) {
        }

        member.ifPresentOrElse((m) -> {
            final var index = m.indexOf("*");
            final var searchForMember = index == -1 ? m : m.substring(0, index);
            if (m.equals(searchForMember)) {
                members = members.stream()
                                .filter(i -> i.getMember().orElse("")
                                .equals(searchForMember))
                                .collect(Collectors.toList());
            } else {
                members = members.stream()
                                .filter(i -> i.getMember().orElse("")
                                .startsWith(searchForMember))
                                .collect(Collectors.toList());
            }
        }, () -> displayDataSets(dataSets, dataSet, isColumnView, isAttributes));
        final var membersSize = members.size();
        if (member.isPresent() && membersSize == 0) {
            terminal.println(Constants.NO_MEMBERS);
            return;
        }
        displayListStatus(membersSize, dataSets.size());

        if (!isColumnView) {  // ls -l
            displayMembers(members, isAttributes);
            return;
        }

        if (membersSize == 0) {
            return;
        }

        // ls
        final var line = new StringBuilder();
        for (final var item : members) {
            line.append(String.format("%-8s", item.getMember().orElse("")));
            line.append(" ");
        }
        terminal.println(line.toString());
    }

    private List<Member> getMembers(String dataSet) throws ExecutionException, InterruptedException, TimeoutException {
        LOG.debug("*** getMembers ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureDsnMembers(zosDsnList, dataSet, params));
        return submit.get(timeout, TimeUnit.SECONDS);
    }

    private List<Dataset> getDataSets(String dataSet) throws ExecutionException, InterruptedException, TimeoutException {
        LOG.debug("*** getDataSets ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureListDsn(zosDsnList, dataSet, params));
        return submit.get(timeout, TimeUnit.SECONDS);
    }

    private void displayListStatus(int membersSize, int dataSetsSize) {
        LOG.debug("*** displayListStatus ***");
        if (membersSize == 0 && dataSetsSize == 1) {
            terminal.println(Constants.NO_MEMBERS);
        }
        if (membersSize == 0 && dataSetsSize == 0) {
            terminal.println(Constants.NO_LISTING);
        }
    }

    private void displayDataSets(List<Dataset> dataSets, String ignoreCurrDataSet,
                                 boolean isColumnView, boolean isAttributes) {
        LOG.debug("*** displayDataSets ***");
        if (dataSets.isEmpty() || (dataSets.size() == 1
                && ignoreCurrDataSet.equalsIgnoreCase(dataSets.get(0).getDsname().orElse("")))) {
            return;
        }
        if (!isColumnView && isAttributes) { // ls -l
            final var columnFormat = "%-11s %-11s %-8s %-5s %-5s %-6s %-7s %-5s";
            terminal.println(String.format(columnFormat,
                    "cdate", "rdate", "vol", "dsorg", "recfm", "blksz", "dsntp", "dsname"));
            dataSets.forEach(ds -> {
                final var dsname = ds.getDsname().orElse("n\\a");
                if (!dsname.equalsIgnoreCase(ignoreCurrDataSet)) {
                    terminal.println(String.format(columnFormat, ds.getCdate().orElse("n\\a"),
                            ds.getRdate().orElse("n\\a"), ds.getVol().orElse("n\\a"),
                            ds.getDsorg().orElse("n\\a"), ds.getRecfm().orElse("n\\a"),
                            ds.getBlksz().orElse("n\\a"), ds.getDsntp().orElse("n\\a"),
                            dsname));
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

    private void displayMembers(List<Member> members, boolean isAttributes) {
        LOG.debug("*** displayMembers ***");
        if (members.isEmpty()) {
            return;
        }
        final var columnFormat = "%-8s %-10s %-10s %-4s %-5s";
        if (isDataSets) {
            terminal.println();
        }
        if (isAttributes) {
            terminal.println(String.format(columnFormat, "user", "cdate", "mdate", "mod", "member"));
            for (final var member : members) {
                terminal.println(String.format(columnFormat, member.getUser().orElse("n\\a"),
                        member.getC4date().orElse("n\\a"), member.getM4date().orElse("n\\a"),
                        member.getMod().orElse(0), member.getMember().orElse("n\\a")));
            }
        } else {
            members.forEach(m -> terminal.println(m.getMember().orElse("")));
        }
    }

}