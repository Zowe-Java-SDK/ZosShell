package zos.shell.service.dsn.delete;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnDelete;

import java.util.concurrent.Callable;

public class FutureDelete extends Delete implements Callable<ResponseStatus> {

    private final String dataset;
    private final String member;

    public FutureDelete(final DsnDelete dsnDelete, final String dataset, final String member) {
        super(dsnDelete);
        this.dataset = dataset;
        this.member = member;
    }

    public FutureDelete(final DsnDelete dsnDelete, final String dataset) {
        super(dsnDelete);
        this.dataset = dataset;
        this.member = "";
    }

    @Override
    public ResponseStatus call() {
        if (member.isBlank()) {
            return this.delete(dataset);
        }
        return this.delete(dataset, member);
    }

}
