package zos.shell.future;

import zos.shell.response.ResponseStatus;
import zos.shell.service.job.TerminateCmd;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;

import java.util.concurrent.Callable;

public class FutureTerminate extends TerminateCmd implements Callable<ResponseStatus> {

    private final TerminateCmd.Type type;
    private final String jobOrTask;

    public FutureTerminate(ZosConnection connection, IssueConsole issueConsole, TerminateCmd.Type type, String jobOrTask) {
        super(connection, issueConsole);
        this.type = type;
        this.jobOrTask = jobOrTask;
    }

    @Override
    public ResponseStatus call() {
        return this.stopOrCancel(type, jobOrTask);
    }

}
