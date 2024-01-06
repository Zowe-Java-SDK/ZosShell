package zos.shell.service.dsn.list;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.datasetlst.DatasetLst;
import zos.shell.service.memberlst.MemberLst;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LstCmd {

    private static final Logger LOG = LoggerFactory.getLogger(LstCmd.class);

    private final TextTerminal<?> terminal;
    private final long timeout;
    private List<Member> members = new ArrayList<>();
    private List<Dataset> datasets = new ArrayList<>();
    private final DsnList dsnList;
    private boolean isDataSets = false;

    public LstCmd(TextTerminal<?> terminal, DsnList dsnList, long timeout) {
        LOG.debug("*** Listing ***");
        this.terminal = terminal;
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public void ls(String memberValue, String dataSet, boolean isColumnView, boolean isAttributes)
            throws ZosmfRequestException {
        LOG.debug("*** ls ***");
        var member = Optional.ofNullable(memberValue);
        if (member.isPresent()) {
            member = Optional.of(memberValue.toUpperCase());
        }

        datasets = getDataSets(dataSet);
        isDataSets = datasets.size() > 1;
        members = getMembers(dataSet);

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
        }, () -> displayDataSets(dataSet, isColumnView, isAttributes));
        final var membersSize = members.size();
        if (member.isPresent() && membersSize == 0) {
            terminal.println(Constants.NO_MEMBERS);
            return;
        }
        displayListStatus(membersSize, datasets.size());

        if (!isColumnView) {  // ls -l
            displayMembers(isAttributes);
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

    private List<Member> getMembers(final String member) throws ZosmfRequestException {
        LOG.debug("*** getMembers ***");
        final var memberLst = new MemberLst(dsnList, timeout);
        return memberLst.memberLst(member);
    }

    private List<Dataset> getDataSets(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** getDataSets ***");
        final var datasetLst = new DatasetLst(dsnList, timeout);
        return datasetLst.datasetLst(dataset);
    }

    private void displayListStatus(final int membersSize, final int dataSetsSize) {
        LOG.debug("*** displayListStatus ***");
        if (membersSize == 0 && dataSetsSize == 1) {
            terminal.println(Constants.NO_MEMBERS);
        }
        if (membersSize == 0 && dataSetsSize == 0) {
            terminal.println(Constants.NO_LISTING);
        }
    }

    private void displayDataSets(final String ignoreCurrDataSet, boolean isColumnView, boolean isAttributes) {
        LOG.debug("*** displayDataSets ***");
        if (this.datasets.isEmpty() || (this.datasets.size() == 1
                && ignoreCurrDataSet.equalsIgnoreCase(this.datasets.get(0).getDsname().orElse("")))) {
            return;
        }
        if (!isColumnView && isAttributes) { // ls -l
            final var columnFormat = "%-11s %-11s %-8s %-5s %-5s %-6s %-7s %-5s";
            terminal.println(String.format(columnFormat,
                    "cdate", "rdate", "vol", "dsorg", "recfm", "blksz", "dsntp", "dsname"));
            this.datasets.forEach(ds -> {
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
            this.datasets.forEach(ds -> {
                final var dsname = ds.getDsname().orElse("");
                if (!dsname.equalsIgnoreCase(ignoreCurrDataSet)) {
                    terminal.println(dsname);
                }
            });
        }
    }

    private void displayMembers(boolean isAttributes) {
        LOG.debug("*** displayMembers ***");
        if (this.members.isEmpty()) {
            return;
        }
        final var columnFormat = "%-8s %-10s %-10s %-4s %-5s";
        if (isDataSets) {
            terminal.println();
        }
        if (isAttributes) {
            terminal.println(String.format(columnFormat, "user", "cdate", "mdate", "mod", "member"));
            for (final var member : this.members) {
                terminal.println(String.format(columnFormat, member.getUser().orElse("n\\a"),
                        member.getC4date().orElse("n\\a"), member.getM4date().orElse("n\\a"),
                        member.getMod().orElse(0), member.getMember().orElse("n\\a")));
            }
        } else {
            this.members.forEach(m -> terminal.println(m.getMember().orElse("")));
        }
    }

}