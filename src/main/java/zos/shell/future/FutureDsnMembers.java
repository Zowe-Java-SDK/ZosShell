package zos.shell.future;

import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureDsnMembers implements Callable<List<Member>> {

    private final String dataSet;
    private final DsnList dsnList;
    private final ListParams params;

    public FutureDsnMembers(DsnList dsnList, String dataSet, ListParams params) {
        this.dsnList = dsnList;
        this.dataSet = dataSet;
        this.params = params;
    }

    @Override
    public List<Member> call() throws Exception {
        return dsnList.getMembers(dataSet, params);
    }

}
