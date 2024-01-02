package zos.shell.service.dsn;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.Response;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnDelete;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DeleteCmd {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteCmd.class);

    private final TextTerminal<?> terminal;
    private final DsnDelete dsnDelete;
    private final DsnList dsnList;
    private final ListParams params = new ListParams.Builder().build();

    public DeleteCmd(TextTerminal<?> terminal, DsnDelete dsnDelete, DsnList dsnList) {
        LOG.debug("*** Delete ***");
        this.terminal = terminal;
        this.dsnDelete = dsnDelete;
        this.dsnList = dsnList;
    }

    public void rm(final String currDataSet, final String param) {
        LOG.debug("*** rm ***");
        try {
            List<Member> members = new ArrayList<>();

            if (param.contains("*") && param.chars().filter(ch -> ch == '*').count() == 1) {
                String lookForStr = "";

                if (param.length() > 1) {
                    final var index = param.indexOf('*');
                    lookForStr = param.substring(0, index).toUpperCase();
                }

                if (isCurrDataSetEmpty(currDataSet)) {
                    return;
                }
                try {
                    members = dsnList.getMembers(currDataSet, params);
                } catch (ZosmfRequestException e) {
                    final String errMsg = Util.getResponsePhrase(e.getResponse());
                    terminal.println(errMsg != null ? errMsg : e.getMessage());
                }

                if (!lookForStr.isEmpty()) {
                    String finalLookForStr = lookForStr;
                    members = members.stream()
                            .filter(i -> i.getMember().orElse("")
                            .startsWith(finalLookForStr))
                            .collect(Collectors.toList());
                }

                final var membersDeleted = new StringBuilder();
                final var membersNotDeleted = new StringBuilder();
                final var success = new AtomicBoolean(true);
                members.forEach(m -> {
                    try {
                        dsnDelete.delete(currDataSet, m.getMember().orElse(""));
                        success.set(true);
                    } catch (ZosmfRequestException e) {
                        success.set(false);
                        final String errMsg = Util.getResponsePhrase(e.getResponse());
                        terminal.println(errMsg != null ? errMsg : e.getMessage());
                    }
                    if (success.get()) {
                        membersDeleted.append(m.getMember().orElse("n\\a"));
                        membersDeleted.append(" ");
                    } else {
                        membersNotDeleted.append(m.getMember().orElse("n\\a"));
                        membersNotDeleted.append(" ");
                    }
                });
                if (!members.isEmpty()) {
                    terminal.println(membersDeleted + "successfully deleted...");
                    if (!membersNotDeleted.toString().isEmpty()) {
                        terminal.println(membersDeleted + "not deleted...");
                    }
                } else {
                    terminal.println(Constants.DELETE_NOTHING_ERROR);
                }
                return;
            }

            if (Util.isMember(param)) {
                if (isCurrDataSetEmpty(currDataSet)) {
                    return;
                }
                try {
                    members = dsnList.getMembers(currDataSet, params);
                    if (members.stream().noneMatch(m -> param.equalsIgnoreCase(m.getMember().orElse("")))) {
                        terminal.println(Constants.DELETE_NOTHING_ERROR);
                        return;
                    }
                    if (performMemberDeleteCheckFailedResponse(currDataSet, param)) {
                        return;
                    }
                } catch (ZosmfRequestException e) {
                    final String errMsg = Util.getResponsePhrase(e.getResponse());
                    terminal.println(errMsg != null ? errMsg : e.getMessage());
                    return;
                }
                terminal.println(param.toUpperCase() + " successfully deleted...");
                return;
            }

            if (param.contains("(") && param.contains(")")) {
                final var dataSetMember = Util.getDatasetAndMember(param);
                if (dataSetMember == null) {
                    terminal.println(Constants.DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR);
                    return;
                }

                try {
                    var response = dsnDelete.delete(dataSetMember.getDataSet(), dataSetMember.getMember());
                    if (failed(response)) {
                        return;
                    }
                } catch (ZosmfRequestException e) {
                    final String errMsg = Util.getResponsePhrase(e.getResponse());
                    terminal.println(errMsg != null ? errMsg : e.getMessage());
                    return;
                }
                terminal.println(param.toUpperCase() + " successfully deleted...");
                return;
            }

            if (Util.isDataSet(param)) {
                if (performDatasetDeleteCheckFailedResponse(param)) {
                    return;
                }
                terminal.println(param.toUpperCase() + " successfully deleted...");
                return;
            }
        } catch (ZosmfRequestException e) {
            terminal.println(e.getMessage());
            return;
        }

        terminal.println(Constants.DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR);
    }

    private boolean performMemberDeleteCheckFailedResponse(String currDataSet, String memberName)
            throws ZosmfRequestException {
        LOG.debug("*** performMemberDeleteCheckFailedResponse ***");
        final var response = dsnDelete.delete(currDataSet, memberName);
        return failed(response);
    }

    private boolean performDatasetDeleteCheckFailedResponse(String datasetName) throws ZosmfRequestException {
        LOG.debug("*** performDatasetDeleteCheckFailedResponse ***");
        final var response = dsnDelete.delete(datasetName);
        return failed(response);
    }

    private boolean isCurrDataSetEmpty(String currDataSet) {
        LOG.debug("*** isCurrDataSetEmpty ***");
        if (currDataSet.isBlank()) {
            terminal.println(Constants.DELETE_NOTHING_ERROR);
            return true;
        }
        return false;
    }

    private boolean failed(Response response) {
        LOG.debug("*** failed ***");
        final var code = response.getStatusCode().orElse(-1);
        if (Util.isHttpError(code)) {
            terminal.println("delete operation failed with http code + " + code + ", try again...");
            return true;
        }
        return false;
    }

}
