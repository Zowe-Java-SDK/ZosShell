package com.commands;

import com.Constants;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosconsole.IssueCommand;
import zowe.client.sdk.zosconsole.input.IssueParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MvsCommand {

    private final TextTerminal<?> terminal;
    private final IssueCommand issueCommand;

    public MvsCommand(TextTerminal<?> terminal, IssueCommand issueCommand) {
        this.terminal = terminal;
        this.issueCommand = issueCommand;
    }

    public void executeCommand(String command) {
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(command);
        try {
            while (m.find()) {
                command = m.group(1);
            }
            var params = new IssueParams();
            params.setCommand(command);
            issueCommand.issue(params);
        } catch (Exception e) {
            terminal.println(e.getMessage());
            terminal.println(Constants.MVS_EXECUTION_ERROR_MSG);
            return;
        }
        terminal.println(Constants.MVS_EXECUTION_SUCCESS_MSG);
    }

}
