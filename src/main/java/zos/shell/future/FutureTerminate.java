package zos.shell.future;

import zos.shell.commands.Terminate;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;

import java.util.concurrent.Callable;

public class FutureTerminate extends Terminate implements Callable<ResponseStatus> {

    private final Terminate.Type type;
    private final String jobOrTask;

    public FutureTerminate(ZosConnection connection, IssueConsole issueConsole, Terminate.Type type, String jobOrTask) {
        super(connection, issueConsole);
        this.type = type;
        this.jobOrTask = jobOrTask;
    }

    @Override
    public ResponseStatus call() {
        return this.stopOrCancel(type, jobOrTask);
    }

}
