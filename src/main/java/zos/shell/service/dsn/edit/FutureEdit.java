package zos.shell.service.dsn.edit;

import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.DownloadCmd;

import java.util.concurrent.Callable;

public class FutureEdit extends Edit implements Callable<ResponseStatus> {

    private final String dataset;
    private final String memberOrDataset;

    public FutureEdit(final DownloadCmd download, final String dataset, final String memberOrDataset) {
        super(download);
        this.dataset = dataset;
        this.memberOrDataset = memberOrDataset;
    }

    @Override
    public ResponseStatus call() {
        return this.open(this.dataset, this.memberOrDataset);
    }

}
