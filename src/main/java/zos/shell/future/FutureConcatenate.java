package zos.shell.future;

import zos.shell.service.dsn.ConcatCmd;
import zos.shell.service.dsn.DownloadCmd;
import zos.shell.response.ResponseStatus;

import java.util.concurrent.Callable;

public class FutureConcatenate extends ConcatCmd implements Callable<ResponseStatus> {

    private final String currDataSet;
    private final String target;

    public FutureConcatenate(DownloadCmd download, String currDataSet, String target) {
        super(download);
        this.currDataSet = currDataSet;
        this.target = target;
    }

    @Override
    public ResponseStatus call() {
        return this.cat(currDataSet, target);
    }

}
