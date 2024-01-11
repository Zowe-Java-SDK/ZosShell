package zos.shell.service.console;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;

import java.util.concurrent.Callable;

public class FutureConsole extends Console implements Callable<ResponseStatus> {

    private final String command;

    public FutureConsole(final ZosConnection connection, final IssueConsole issueConsole, final String command) {
        super(connection, issueConsole);
        this.command = command;
    }

    @Override
    public ResponseStatus call() {
        return this.issueConsole(command);
    }

}
