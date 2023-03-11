package zos.shell.future;

import zos.shell.commands.MakeDirectory;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.ZosDsn;
import zowe.client.sdk.zosfiles.input.CreateParams;

import java.util.concurrent.Callable;

public class FutureMakeDirectory extends MakeDirectory implements Callable<ResponseStatus> {

    private final String dataset;
    private final CreateParams params;

    public FutureMakeDirectory(ZosDsn zosDsn, String dataset, CreateParams params) {
        super(zosDsn);
        this.dataset = dataset;
        this.params = params;
    }

    @Override
    public ResponseStatus call() throws Exception {
        return this.mkdir(dataset, params);
    }

}
