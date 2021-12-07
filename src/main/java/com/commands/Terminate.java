package com.commands;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosconsole.ConsoleResponse;
import zosconsole.IssueCommand;
import zosconsole.input.IssueParams;

public class Terminate {

    private final TextTerminal<?> terminal;
    private final IssueCommand issueCommand;

    public enum Type {
        STOP,
        CANCEL
    }

    public Terminate(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.issueCommand = new IssueCommand(connection);
    }

    public void kill(Type type, String param) {
        final var params = new IssueParams();
        switch (type) {
            case STOP:
                params.setCommand("P " + param);
                break;
            case CANCEL:
                params.setCommand("C " + param);
                break;
            default:
                terminal.println("invalid terminate type, try again...");
                return;
        }
        ConsoleResponse response;
        try {
            response = issueCommand.issue(params);
            var result = response.getCommandResponse().orElse(null);
            if (result == null) {
                terminal.println("no response from " + (type == Type.STOP ? "stop" : "cancel") + " command, try again...");
                return;
            }
            // remove last newline i.e. \n
            terminal.println(result.substring(0, result.length() - 1));
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
    }

}
