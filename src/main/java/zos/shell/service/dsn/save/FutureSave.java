package zos.shell.service.dsn.save;

import zos.shell.response.ResponseStatus;
import zos.shell.service.checksum.CheckSumService;
import zos.shell.service.path.PathService;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.Callable;

public class FutureSave extends Save implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String memberOrDataset;

    public FutureSave(final DsnWrite dsnWrite, final PathService pathService, final CheckSumService checkSumService,
                      final String dataSet, final String memberOrDataset) {
        super(dsnWrite, pathService, checkSumService);
        this.dataSet = dataSet;
        this.memberOrDataset = memberOrDataset;
    }

    @Override
    public ResponseStatus call() {
        return this.save(dataSet, memberOrDataset);
    }

}
