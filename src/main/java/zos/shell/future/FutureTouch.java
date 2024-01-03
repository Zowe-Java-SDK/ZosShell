package zos.shell.future;

import zos.shell.dto.Member;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.TouchCmd;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.Callable;

public class FutureTouch extends TouchCmd implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureTouch(DsnWrite dsnWrite, Member members, String dataSet, String member) {
        super(dsnWrite, members);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.touch(this.dataSet, this.member);
    }

}
