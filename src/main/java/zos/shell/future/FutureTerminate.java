package zos.shell.future;

import zos.shell.commands.Terminate;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosconsole.IssueCommand;

import java.util.concurrent.Callable;

public class FutureTerminate extends Terminate implements Callable<ResponseStatus> {

    private final Terminate.Type type;
    private final String jobOrTask;

    public FutureTerminate(IssueCommand issueCommand, Terminate.Type type, String jobOrTask) {
        super(issueCommand);
        this.type = type;
        this.jobOrTask = jobOrTask;
    }

    @Override
    public ResponseStatus call() {
        return this.stopOrCancel(type, jobOrTask);
    }

}
