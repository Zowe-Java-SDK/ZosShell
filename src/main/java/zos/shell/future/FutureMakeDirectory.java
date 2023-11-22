package zos.shell.future;

import zos.shell.commands.MakeDirectory;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.concurrent.Callable;

public class FutureMakeDirectory extends MakeDirectory implements Callable<ResponseStatus> {

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
