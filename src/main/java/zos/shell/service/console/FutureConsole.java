package zos.shell.service.console;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosconsole.method.ConsoleCmd;

import java.util.concurrent.Callable;

public class FutureConsole extends Console implements Callable<ResponseStatus> {

    private final String command;

    public FutureConsole(final ConsoleCmd issueConsole, final String consoleName, final String command) {
        super(issueConsole, consoleName);
        this.command = command;
    }

    @Override
    public ResponseStatus call() {
        return this.issueConsole(command);
    }

}
