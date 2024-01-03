package zos.shell.service.dsn.touch;

import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;
import zowe.client.sdk.zosfiles.dsn.types.AttributeType;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureMemberLst implements Callable<List<Member>> {

    private final String dataset;
    private final DsnList dsnList;

    public FutureMemberLst(final DsnList dsnList, final String dataset) {
        this.dsnList = dsnList;
        this.dataset = dataset;
    }

    @Override
    public List<Member> call() throws Exception {
        return dsnList.getMembers(dataset, new ListParams.Builder().attribute(AttributeType.MEMBER).build());
    }

}