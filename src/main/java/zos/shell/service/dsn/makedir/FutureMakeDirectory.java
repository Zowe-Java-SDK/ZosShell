package zos.shell.service.dsn.makedir;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.input.DsnCreateInputData;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.concurrent.Callable;

public class FutureMakeDirectory extends MakeDirectory implements Callable<ResponseStatus> {

    private final String dataset;
    private final DsnCreateInputData params;

    public FutureMakeDirectory(final DsnCreate dsnCreate, final String dataset, final DsnCreateInputData params) {
        super(dsnCreate);
        this.dataset = dataset;
        this.params = params;
    }

    @Override
    public ResponseStatus call() {
        return this.create(dataset, params);
    }

}
