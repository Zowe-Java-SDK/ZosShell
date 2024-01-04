package zos.shell.service.datasetlst;

import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;
import zowe.client.sdk.zosfiles.dsn.types.AttributeType;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureDatasetLst implements Callable<List<Dataset>> {

    private final String dataset;
    private final DsnList dsnList;

    public FutureDatasetLst(final DsnList dsnList, final String dataset) {
        this.dsnList = dsnList;
        this.dataset = dataset;
    }

    @Override
    public List<Dataset> call() throws Exception {
        return dsnList.getDatasets(dataset, new ListParams.Builder().attribute(AttributeType.BASE).build());
    }

}
