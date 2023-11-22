package zos.shell.future;

import zos.shell.commands.Count;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;

import java.util.concurrent.Callable;

public class FutureCount extends Count implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String filter;

    public FutureCount(DsnList DsnList, String dataSet, String filter) {
        super(DsnList);
        this.dataSet = dataSet;
        this.filter = filter;
    }

    @Override
    public ResponseStatus call() {
        return this.count(dataSet, filter);
    }

}
