package zos.shell.future;

import zos.shell.commands.Download;
import zos.shell.commands.Vi;
import zos.shell.dto.ResponseStatus;

import java.util.concurrent.Callable;

public class FutureVi extends Vi implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureVi(Download download, String dataSet, String member) {
        super(download);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.vi(this.dataSet, this.member);
    }

}
