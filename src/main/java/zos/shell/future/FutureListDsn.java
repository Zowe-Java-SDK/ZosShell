package zos.shell.future;

import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureListDsn implements Callable<List<Dataset>> {

    private final DsnList dsnList;
    private final String dataSet;
    private final ListParams params;

    public FutureListDsn(DsnList dsnList, String dataSet, ListParams params) {
        this.dsnList = dsnList;
        this.dataSet = dataSet;
        this.params = params;
    }

    @Override
    public List<Dataset> call() throws Exception {
        return dsnList.getDatasets(dataSet, params);
    }

}
