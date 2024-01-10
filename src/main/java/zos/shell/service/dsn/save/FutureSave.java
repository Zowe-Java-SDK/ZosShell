package zos.shell.service.dsn.save;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.Callable;

public class FutureSave extends Save implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String memberOrDataset;

    public FutureSave(final DsnWrite dsnWrite, final String dataSet, final String memberOrDataset) {
        super(dsnWrite);
        this.dataSet = dataSet;
        this.memberOrDataset = memberOrDataset;
    }

    @Override
    public ResponseStatus call() {
        return this.save(dataSet, memberOrDataset);
    }

}
