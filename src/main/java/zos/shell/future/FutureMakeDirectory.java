package zos.shell.future;

import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.MakeDirCmd;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.concurrent.Callable;

public class FutureMakeDirectory extends MakeDirCmd implements Callable<ResponseStatus> {

    private final String dataset;
    private final CreateParams params;

    public FutureMakeDirectory(DsnCreate dsnCreate, String dataset, CreateParams params) {
        super(dsnCreate);
        this.dataset = dataset;
        this.params = params;
    }

    @Override
    public ResponseStatus call() {
        return this.mkdir(dataset, params);
    }

}
