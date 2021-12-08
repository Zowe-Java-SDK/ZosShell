package com.commands;

import org.beryx.textio.TextTerminal;
import zosconsole.IssueCommand;

public class Stop extends Terminate {

    public Stop(TextTerminal<?> terminal, IssueCommand issueCommand) {
        super(terminal, issueCommand);
    }

    public void stop(String param) {
        this.kill(Type.STOP, param);
    }

}
