package zos.shell.future;

import org.beryx.textio.TextTerminal;
import zos.shell.commands.Tail;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosjobs.GetJobs;

import java.util.concurrent.Callable;

public class FutureTailJob extends Tail implements Callable<ResponseStatus> {

    private final String[] params;

    public FutureTailJob(TextTerminal<?> terminal, ZOSConnection connection, boolean isAll, long timeout,
                         String[] params) {
        super(terminal, new GetJobs(connection), isAll, timeout);
        this.params = params;
    }

    @Override
    public ResponseStatus call() {
        return this.tail(params);
    }

}
