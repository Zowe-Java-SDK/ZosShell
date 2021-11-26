package com.command;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsnCopy;

public class Copy {

    private final TextTerminal<?> terminal;
    private final ZOSConnection connection;

    public Copy(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.connection = connection;
    }

    public void copy(String currDataSet, String[] params) {
        try {
            final var zosDsnCopy = new ZosDsnCopy(connection);

            var fromDataSetName = "";
            var toDataSetName = "";
            boolean copyAllMembers = false;

            String param1 = params[1].toUpperCase();
            String param2 = params[2].toUpperCase();

            if (Util.isMember(param1)) {
                fromDataSetName = currDataSet + "(" + param1 + ")";
            }

            if (Util.isMember(param2)) {
                toDataSetName = currDataSet + "(" + param2 + ")";
            }

            if (".".equals(param1) && ".".equals(param2)) {
                terminal.printf(Constants.INVALID_COMMAND + "\n");
                return;
            }

            if (".".equals(param1)) {
                fromDataSetName = currDataSet;
                if (Util.isDataSet(param2))
                    toDataSetName = param2;
                else {
                    terminal.printf("second argument invalid for copy all operation, try again...\n");
                    return;
                }
                copyAllMembers = true;
            }

            if (".".equals(param2)) {
                if (Util.isMember(param1)) {
                    terminal.printf(Constants.COPY_OPS_ITSELF_ERROR + "\n");
                    return;
                }

                if (Util.isDataSet(param1)) {
                    terminal.printf(Constants.COPY_OPS_NO_MEMBER_ERROR + "\n");
                    return;
                }

                if (param1.contains(currDataSet)) {
                    terminal.printf(Constants.COPY_OPS_ITSELF_ERROR + "\n");
                    return;
                }

                if (param1.contains("(") && param1.contains(")")) {
                    String member;
                    String dataset;

                    int index = param1.indexOf("(");
                    dataset = param1.substring(0, index);
                    if (!Util.isDataSet(dataset)) {
                        terminal.printf(Constants.COPY_OPS_NO_MEMBER_AND_DATASET_ERROR + "\n");
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

            zosDsnCopy.copy(fromDataSetName, toDataSetName, true, copyAllMembers);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.printf(Constants.SEVERE_ERROR + "\n");
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
    }

}
