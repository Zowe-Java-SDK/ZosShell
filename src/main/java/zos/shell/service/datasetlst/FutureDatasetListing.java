package zos.shell.service.datasetlst;

import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;
import zowe.client.sdk.zosfiles.dsn.types.AttributeType;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureDatasetListing implements Callable<List<Dataset>> {

    private final String dataset;
    private final DsnList dsnList;
    private final long timeout;

    public FutureDatasetListing(final DsnList dsnList, final String dataset, final long timeout) {
        this.dsnList = dsnList;
        this.dataset = dataset;
        this.timeout = timeout;
    }

    @Override
    public List<Dataset> call() throws Exception {
        return dsnList.getDatasets(dataset,
                new ListParams.Builder().attribute(AttributeType.BASE)
                        .maxLength("0")  // return all
                        .responseTimeout(String.valueOf(this.timeout)).build());
    }

}
