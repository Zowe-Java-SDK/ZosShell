package zos.shell.service.rename;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnRename;

import java.util.concurrent.Callable;

public class FutureRenameDataset extends RenameDataset implements Callable<ResponseStatus> {

    private final String source;
    private final String destination;

    public FutureRenameDataset(final DsnRename dsnRename, final String source, final String destination) {
        super(dsnRename);
        this.source = source;
        this.destination = destination;
    }

    @Override
    public ResponseStatus call() throws ZosmfRequestException {
        return this.renameDataset(source, destination);
    }

}
