package zos.shell.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;

import java.util.ArrayList;
import java.util.List;

public class Member {

    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Member(ZosDsnList zosDsnList) {
        this.zosDsnList = zosDsnList;
    }

    public List<String> getMembers(String dataSet) throws Exception {
        List<zowe.client.sdk.zosfiles.response.Member> members = zosDsnList.listDsnMembers(dataSet, params);
        final var memberNames = new ArrayList<String>();
        members.forEach(m -> memberNames.add(m.getMember().orElse("")));
        return memberNames;
    }

    @Override
    public String toString() {
        return "Member{" +
                "zosDsnList=" + zosDsnList +
                ", params=" + params +
                '}';
    }

}
