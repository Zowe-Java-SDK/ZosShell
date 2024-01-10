package zos.shell.service.dsn.touch;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.Callable;

public class FutureTouch extends Touch implements Callable<ResponseStatus> {

    private final String dataset;
    private final String member;

    public FutureTouch(final DsnWrite dsnWrite, final String dataset, final String member) {
        super(dsnWrite);
        this.dataset = dataset;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.create(this.dataset, this.member);
    }

}
