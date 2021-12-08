package com.commands;

import com.Constants;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsnCopy;

public class Copy {

    private final TextTerminal<?> terminal;
    private final ZosDsnCopy zosDsnCopy;

    public Copy(TextTerminal<?> terminal, ZosDsnCopy zosDsnCopy) {
        this.terminal = terminal;
        this.zosDsnCopy = zosDsnCopy;
    }

    public void copy(String currDataSet, String[] params) {
        try {
            var fromDataSetName = "";
            var toDataSetName = "";
            var copyAllMembers = false;

            String param1 = params[1].toUpperCase();
            String param2 = params[2].toUpperCase();

            if (Util.isMember(param1)) {
                fromDataSetName = currDataSet + "(" + param1 + ")";
            }

            if (Util.isMember(param2)) {
                toDataSetName = currDataSet + "(" + param2 + ")";
            }

            if (".".equals(param1) && ".".equals(param2)) {
                terminal.println(Constants.INVALID_COMMAND);
                return;
            }

            if (".".equals(param1)) {
                fromDataSetName = currDataSet;
                if (Util.isDataSet(param2))
                    toDataSetName = param2;
                else {
                    terminal.println("second argument invalid for copy all operation, try again...");
                    return;
                }
                copyAllMembers = true;
            }

            if (".".equals(param2)) {
                if (Util.isMember(param1)) {
                    terminal.println(Constants.COPY_OPS_ITSELF_ERROR);
                    return;
                }

                if (Util.isDataSet(param1)) {
                    terminal.println(Constants.COPY_OPS_NO_MEMBER_ERROR);
                    return;
                }

                if (param1.contains(currDataSet)) {
                    terminal.println(Constants.COPY_OPS_ITSELF_ERROR);
                    return;
                }

                if (param1.contains("(") && param1.contains(")")) {
                    String member;
                    String dataset;

                    int index = param1.indexOf("(");
                    dataset = param1.substring(0, index);
                    if (!Util.isDataSet(dataset)) {
                        terminal.println(Constants.COPY_OPS_NO_MEMBER_AND_DATASET_ERROR);
                        return;
                    }

                    member = param1.substring(index + 1, param1.length() - 1);
                    fromDataSetName = param1;
                    toDataSetName = currDataSet + "(" + member + ")";
                }
            }

            if (Util.isMember(param1) && Util.isDataSet(param2)) {
                fromDataSetName = currDataSet + "(" + param1 + ")";
                toDataSetName = param2 + "(" + param1 + ")";
            }

            if (fromDataSetName.isEmpty())
                fromDataSetName = param1;

            if (toDataSetName.isEmpty())
                toDataSetName = param2;

            var response = zosDsnCopy.copy(fromDataSetName, toDataSetName, true, copyAllMembers);
            var code = response.getStatusCode().orElse(-1);
            if (Util.isHttpError(code)) {
                terminal.println("copy operation failed with http code + " + code + ", try again...");
                return;
            }
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
            return;
        }
        terminal.println("copy operation succeeded...");
    }

}
