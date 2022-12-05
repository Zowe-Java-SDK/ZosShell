package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import zos.shell.Constants;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.Response;
import zowe.client.sdk.zosfiles.ZosDsn;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Delete {

    private final TextTerminal<?> terminal;
    private final ZosDsn zosDsn;
    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Delete(TextTerminal<?> terminal, ZosDsn zosDsn, ZosDsnList zosDsnList) {
        this.terminal = terminal;
        this.zosDsn = zosDsn;
        this.zosDsnList = zosDsnList;
    }

    public void rm(String currDataSet, String param) {
        try {
            List<String> members = new ArrayList<>();

            if (param.contains("*")) {
                String lookForStr = "";

                if (param.length() > 1) {
                    int index = param.indexOf('*');
                    lookForStr = param.substring(0, index).toUpperCase();
                }

                if (isCurrDataSetEmpty(currDataSet)) {
                    return;
                }
                try {
                    members = zosDsnList.listDsnMembers(currDataSet, params);
                } catch (Exception e) {
                    Util.printError(terminal, e.getMessage());
                }

                if (!lookForStr.isEmpty()) {
                    String finalLookForStr = lookForStr;
                    members = members.stream().filter(i -> i.startsWith(finalLookForStr)).collect(Collectors.toList());
                }

                StringBuilder membersDeleted = new StringBuilder();
                StringBuilder membersNotDeleted = new StringBuilder();
                final var success = new AtomicBoolean(true);
                members.forEach(m -> {
                    try {
                        zosDsn.deleteDsn(currDataSet, m);
                        success.set(true);
                    } catch (Exception e) {
                        success.set(false);
                        terminal.println(e + "");
                    }
                    if (success.get()) {
                        membersDeleted.append(m);
                        membersDeleted.append(" ");
                    } else {
                        membersNotDeleted.append(m);
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
                    members = zosDsnList.listDsnMembers(currDataSet, params);
                    if (members.stream().noneMatch(param::equalsIgnoreCase)) {
                        terminal.println(Constants.DELETE_NOTHING_ERROR);
                        return;
                    }
                    if (performDeleteCheckFailedResponse(currDataSet, param)) {
                        return;
                    }
                } catch (Exception e) {
                    Util.printError(terminal, e.getMessage());
                    return;
                }
                terminal.println(param.toUpperCase() + " successfully deleted...");
                return;
            }

            if (param.contains("(") && param.contains(")")) {
                final var dataSetMember = Util.getMemberFromDataSet(param);
                if (dataSetMember == null) {
                    terminal.println(Constants.DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR);
                    return;
                }

                try {
                    var response = zosDsn.deleteDsn(dataSetMember.getDataSet(), dataSetMember.getMember());
                    if (failed(response)) {
                        return;
                    }
                } catch (Exception e) {
                    Util.printError(terminal, e.getMessage());
                    return;
                }
                terminal.println(param + " successfully deleted...");
                return;
            }

            if (Util.isDataSet(param)) {
                if (performDeleteCheckFailedResponse(currDataSet, param)) {
                    return;
                }
            }
        } catch (Exception e) {
            terminal.println(e.getMessage());
            return;
        }

        terminal.println(Constants.DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR);
    }

    private boolean performDeleteCheckFailedResponse(String currDataSet, String param) throws Exception {
        final var response = zosDsn.deleteDsn(currDataSet, param);
        return failed(response);
    }

    private boolean isCurrDataSetEmpty(String currDataSet) {
        if (currDataSet.isEmpty()) {
            terminal.println(Constants.DELETE_NOTHING_ERROR);
            return true;
        }
        return false;
    }

    private boolean failed(Response response) {
        final var code = response.getStatusCode().orElse(-1);
        if (Util.isHttpError(code)) {
            terminal.println("delete operation failed with http code + " + code + ", try again...");
            return true;
        }
        return false;
    }

}
