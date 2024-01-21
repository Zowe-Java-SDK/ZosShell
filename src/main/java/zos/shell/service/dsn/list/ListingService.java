package zos.shell.service.dsn.list;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.datasetlst.DatasetListingService;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.utility.DsnUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListingService {

    private static final Logger LOG = LoggerFactory.getLogger(ListingService.class);

    private final TextTerminal<?> terminal;
    private final long timeout;
    private List<Member> members = new ArrayList<>();
    private List<Dataset> datasets = new ArrayList<>();
    private final DsnList dsnList;
    private boolean isDatasets = false;

    public ListingService(final TextTerminal<?> terminal, final DsnList dsnList, final long timeout) {
        LOG.debug("*** ListingService ***");
        this.terminal = terminal;
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public void ls(final String memberValue, final String dataset, boolean isColumnView, boolean isAttributes)
            throws ZosmfRequestException {
        LOG.debug("*** ls ***");
        var member = Optional.ofNullable(memberValue);
        if (member.isPresent()) {
            member = Optional.of(memberValue.toUpperCase());
        }

        datasets = getDataSets(dataset);
        isDatasets = datasets.size() > 1;
        members = getMembers(dataset);

        member.ifPresentOrElse((m) -> {
            int index = m.indexOf("*");
            String searchForMember = index == -1 ? m : m.substring(0, index);
            if (m.equals(searchForMember)) {
                members = DsnUtil.getMembersByFilter(searchForMember, members);
            } else {
                members = DsnUtil.getMembersByStartsWithFilter(searchForMember, members);
            }
        }, () -> displayDataSets(dataset, isColumnView, isAttributes));
        int membersSize = members.size();
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
        var line = new StringBuilder();
        for (var item : members) {
            line.append(String.format("%-8s", item.getMember().orElse("")));
            line.append(" ");
        }
        terminal.println(line.toString());
    }

    private List<Member> getMembers(final String member) throws ZosmfRequestException {
        LOG.debug("*** getMembers ***");
        var memberListingService = new MemberListingService(dsnList, timeout);
        return memberListingService.memberLst(member);
    }

    private List<Dataset> getDataSets(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** getDataSets ***");
        var datasetListingService = new DatasetListingService(dsnList, timeout);
        return datasetListingService.datasetLst(dataset);
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
            var columnFormat = "%-11s %-11s %-8s %-5s %-5s %-6s %-7s %-5s";
            terminal.println(String.format(columnFormat,
                    "cdate", "rdate", "vol", "dsorg", "recfm", "blksz", "dsntp", "dsname"));
            this.datasets.forEach(ds -> {
                var dsname = ds.getDsname().orElse("n\\a");
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
                var dsname = ds.getDsname().orElse("");
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
        var columnFormat = "%-8s %-10s %-10s %-4s %-5s";
        if (isDatasets) {
            terminal.println();
        }
        if (isAttributes) {
            terminal.println(String.format(columnFormat, "user", "cdate", "mdate", "mod", "member"));
            for (var member : this.members) {
                terminal.println(String.format(columnFormat, member.getUser().orElse("n\\a"),
                        member.getC4date().orElse("n\\a"), member.getM4date().orElse("n\\a"),
                        member.getMod().orElse(0), member.getMember().orElse("n\\a")));
            }
        } else {
            this.members.forEach(m -> terminal.println(m.getMember().orElse("")));
        }
    }

}