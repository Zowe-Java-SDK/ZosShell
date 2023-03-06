package zos.shell.future;

import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;
import zowe.client.sdk.zosfiles.response.Member;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureDsnMembers implements Callable<List<Member>> {

    private final String dataSet;
    private final ZosDsnList zosDsnList;
    private final ListParams params;

    public FutureDsnMembers(ZosDsnList zosDsnList, String dataSet, ListParams params) {
        this.zosDsnList = zosDsnList;
        this.dataSet = dataSet;
        this.params = params;
    }

    @Override
    public List<Member> call() throws Exception {
        return zosDsnList.listDsnMembers(dataSet, params);
    }

}
