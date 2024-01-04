package zos.shell.service.job.tail;

import org.beryx.textio.TextTerminal;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureTail extends Tail implements Callable<ResponseStatus> {

    private final String[] params;

    public FutureTail(final TextTerminal<?> terminal, final JobGet retrieve, boolean isAll,
                      final long timeout, final String[] params) {
        super(terminal, retrieve, isAll, timeout);
        this.params = params;
    }

    @Override
    public ResponseStatus call() {
        return this.tail(params);
    }

}
