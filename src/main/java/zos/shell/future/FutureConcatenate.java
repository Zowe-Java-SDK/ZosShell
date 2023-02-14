package zos.shell.future;

import zos.shell.commands.Concatenate;
import zos.shell.commands.Download;
import zos.shell.dto.ResponseStatus;

import java.util.concurrent.Callable;

public class FutureConcatenate extends Concatenate implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureConcatenate(Download download, String dataSet, String member) {
        super(download);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.cat(dataSet, member);
    }

}
