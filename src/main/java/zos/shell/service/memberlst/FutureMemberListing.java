package zos.shell.service.memberlst;

import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;
import zowe.client.sdk.zosfiles.dsn.types.AttributeType;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureMemberListing implements Callable<List<Member>> {

    private final String dataset;
    private final DsnList dsnList;
    private final long timeout;

    public FutureMemberListing(final DsnList dsnList, final String dataset, final long timeout) {
        this.dsnList = dsnList;
        this.dataset = dataset;
        this.timeout = timeout;
    }

    @Override
    public List<Member> call() throws Exception {
        return dsnList.getMembers(dataset,
                new ListParams.Builder().attribute(AttributeType.MEMBER)
                        .maxLength("0")  // return all
                        .responseTimeout(String.valueOf(this.timeout)).build());
    }

}
