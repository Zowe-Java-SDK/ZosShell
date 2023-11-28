package zos.shell.future;

import zos.shell.commands.Concatenate;
import zos.shell.commands.Download;
import zos.shell.dto.ResponseStatus;

import java.util.concurrent.Callable;

public class FutureConcatenate extends Concatenate implements Callable<ResponseStatus> {

    private final String currDataSet;
    private final String target;

    public FutureConcatenate(Download download, String currDataSet, String target) {
        super(download);
        this.currDataSet = currDataSet;
        this.target = target;
    }

    @Override
    public ResponseStatus call() {
        return this.cat(currDataSet, target);
    }

}
