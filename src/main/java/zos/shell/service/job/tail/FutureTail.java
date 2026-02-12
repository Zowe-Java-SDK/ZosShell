package zos.shell.service.job.tail;

import org.beryx.textio.TextTerminal;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureTail extends Tail implements Callable<ResponseStatus> {

    private final String target;
    private final int lines;

    public FutureTail(final TextTerminal<?> terminal, final JobGet retrieve,
                      final int lines, final long timeout, final String target) {
        super(terminal, retrieve, timeout);
        this.target = target;
        this.lines = lines;
    }

    @Override
    public ResponseStatus call() {
        return this.tail(target, lines);
    }

}
