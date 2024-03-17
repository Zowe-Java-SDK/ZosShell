package zos.shell.service.dsn.download;

import zos.shell.response.ResponseStatus;
import zos.shell.service.path.PathService;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.util.concurrent.Callable;

public class FutureDatasetDownload extends Download implements Callable<ResponseStatus> {

    private final String dataset;

    public FutureDatasetDownload(final DsnGet download, final PathService pathService, final String dataset,
                                 boolean isBinary) {
        super(download, pathService, isBinary);
        this.dataset = dataset;
    }

    @Override
    public ResponseStatus call() {
        return this.dataset(dataset);
    }

}
