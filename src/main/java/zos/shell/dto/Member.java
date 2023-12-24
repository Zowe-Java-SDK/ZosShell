package zos.shell.dto;

import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;

import java.util.ArrayList;
import java.util.List;

public class Member {

    private final DsnList dsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Member(DsnList dsnList) {
        this.dsnList = dsnList;
    }

    public List<String> getMembers(String dataSet) throws ZosmfRequestException {
        List<zowe.client.sdk.zosfiles.dsn.response.Member> members = dsnList.getMembers(dataSet, params);
        final var memberNames = new ArrayList<String>();
        members.forEach(m -> memberNames.add(m.getMember().orElse("")));
        return memberNames;
    }

    @Override
    public String toString() {
        return "Member{" +
                "DsnList=" + dsnList +
                ", params=" + params +
                '}';
    }

}
