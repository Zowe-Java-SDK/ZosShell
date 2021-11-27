package com.commands;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosconsole.ConsoleResponse;
import zosconsole.IssueCommand;
import zosconsole.input.IssueParams;

public class Cancel {

    private final TextTerminal<?> terminal;
    private final IssueCommand issueCommand;

    public Cancel(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.issueCommand = new IssueCommand(connection);
    }

    public void cancel(String param) {
        final var params = new IssueParams();
        params.setCommand("C " + param);
        ConsoleResponse response;
        try {
            response = issueCommand.issue(params);
            String result = response.getCommandResponse().orElse(null);
            if (result == null) {
                terminal.println("no response from cancel command, try again...");
                return;
            }
            // remove last newline i.e. \n
            terminal.println(result.substring(0, result.length() - 1));
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
    }

}
