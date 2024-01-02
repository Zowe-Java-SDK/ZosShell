package zos.shell.future;

import zos.shell.service.dsn.DownloadCmd;
import zos.shell.service.dsn.ViCmd;
import zos.shell.response.ResponseStatus;

import java.util.concurrent.Callable;

public class FutureVi extends ViCmd implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureVi(DownloadCmd download, String dataSet, String member) {
        super(download);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.vi(this.dataSet, this.member);
    }

}
