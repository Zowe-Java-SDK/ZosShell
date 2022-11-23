package zos.shell.future;

import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureDsnMembers implements Callable<List<String>> {

    private final String dataSet;
    private final ZosDsnList zosDsnList;
    private final ListParams params;

    public FutureDsnMembers(ZosDsnList zosDsnList, String dataSet, ListParams params) {
        this.zosDsnList = zosDsnList;
        this.dataSet = dataSet;
        this.params = params;
    }

    @Override
    public List<String> call() throws Exception {
        return zosDsnList.listDsnMembers(dataSet, params);
    }

}
