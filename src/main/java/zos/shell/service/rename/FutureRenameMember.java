package zos.shell.service.rename;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnRename;

import java.util.concurrent.Callable;

public class FutureRenameMember extends RenameMember implements Callable<ResponseStatus> {

    private final String dataset;
    private final String source;
    private final String destination;

    public FutureRenameMember(final DsnRename dsnRename, final String dataset, final String source,
                              final String destination) {
        super(dsnRename);
        this.dataset = dataset;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public ResponseStatus call() throws ZosmfRequestException {
        return this.renameMember(dataset, source, destination);
    }

}