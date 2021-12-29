package com.commands;

import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosconsole.IssueCommand;

public class Cancel extends Terminate {

    public Cancel(TextTerminal<?> terminal, IssueCommand issueCommand) {
        super(terminal, issueCommand);
    }

    public void cancel(String param) {
        this.kill(Type.CANCEL, param);
    }

}
