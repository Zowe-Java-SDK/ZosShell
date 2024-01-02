package zos.shell.future;

import zos.shell.service.dsn.SaveCmd;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.Callable;

public class FutureSave extends SaveCmd implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureSave(DsnWrite dsnWrite, String dataSet, String member) {
        super(dsnWrite);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.save(dataSet, member);
    }

}
