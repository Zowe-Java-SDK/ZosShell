package zos.shell.service.dsn.makedir;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.concurrent.Callable;

public class FutureMakeDir extends MakeDir implements Callable<ResponseStatus> {

    private final String dataset;
    private final CreateParams params;

    public FutureMakeDir(final DsnCreate dsnCreate, final String dataset, final CreateParams params) {
        super(dsnCreate);
        this.dataset = dataset;
        this.params = params;
    }

    @Override
    public ResponseStatus call() {
        return this.create(dataset, params);
    }

}
