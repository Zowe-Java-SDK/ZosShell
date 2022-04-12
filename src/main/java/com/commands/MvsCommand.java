package com.commands;

import com.Constants;
import com.config.MvsConsoles;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosconsole.ConsoleResponse;
import zowe.client.sdk.zosconsole.IssueCommand;
import zowe.client.sdk.zosconsole.input.IssueParams;

import java.util.regex.Pattern;

public class MvsCommand {

    private final TextTerminal<?> terminal;
    private final IssueCommand issueCommand;
    private final ZOSConnection connection;
    private final MvsConsoles mvsConsoles = new MvsConsoles();

    public MvsCommand(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.connection = connection;
        this.issueCommand = new IssueCommand(connection);
    }

    public void executeCommand(String command) {
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC_OSX) {
            terminal.println(Constants.OS_ERROR);
            return;
        }

        var p = Pattern.compile("\"([^\"]*)\"");
        var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }

        ConsoleResponse response = null;
        var params = new IssueParams();
        params.setCommand(command);
        var mvsConsoleName = mvsConsoles.getConsoleName(connection.getHost());
        if (mvsConsoleName != null) {
            params.setConsoleName(mvsConsoleName);
        }
        try {
            response = execute(params);
        } catch (Exception ignored) {
        }

        if (response == null) {
            params.setConsoleName(null);
            try {
                response = execute(params);
            } catch (Exception ignored) {
            }
        }

        if (response == null) {
            terminal.println(Constants.MVS_EXECUTION_ERROR_MSG);
            return;
        }
        terminal.println(Constants.MVS_EXECUTION_SUCCESS);
        terminal.println(response.getCommandResponse().orElse("no response"));
    }

    private ConsoleResponse execute(IssueParams params) throws Exception {
        ConsoleResponse response;
        response = issueCommand.issue(params);
        return response;
    }

}
