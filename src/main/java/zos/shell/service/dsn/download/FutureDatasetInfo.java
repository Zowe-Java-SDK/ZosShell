package zos.shell.service.dsn.download;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.util.concurrent.Callable;

public class FutureDatasetInfo extends DatasetInfo implements Callable<ResponseStatus> {

    private final String dataset;

    public FutureDatasetInfo(final DsnGet dsnGet, final String dataset) {
        super(dsnGet);
        this.dataset = dataset;
    }

    @Override
    public ResponseStatus call() {
        return this.dsInfo(dataset);
    }

}