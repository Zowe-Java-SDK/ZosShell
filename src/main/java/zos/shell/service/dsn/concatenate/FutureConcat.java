package zos.shell.service.dsn.concatenate;

import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.Download;

import java.util.concurrent.Callable;

public class FutureConcat extends Concat implements Callable<ResponseStatus> {

    private final String dataset;
    private final String target;

    public FutureConcat(final Download download, final String dataset, final String target) {
        super(download);
        this.dataset = dataset;
        this.target = target;
    }

    @Override
    public ResponseStatus call() {
        return this.cat(dataset, target);
    }

}
