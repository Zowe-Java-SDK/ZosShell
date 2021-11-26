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
    private final ZOSConnection connection;

    public Cancel(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.connection = connection;
    }

    public void cancel(String param) {
        final var params = new IssueParams();
        params.setCommand("C " + param);
        ConsoleResponse response;
        try {
            final var issueCommand = new IssueCommand(connection);
            response = issueCommand.issue(params);
            String result = response.getCommandResponse().get();
            // remove last newline i.e. \n
            terminal.printf(result.substring(0, result.length() - 1) + "\n");
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.printf(Constants.SEVERE_ERROR + "\n");
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
    }

}
