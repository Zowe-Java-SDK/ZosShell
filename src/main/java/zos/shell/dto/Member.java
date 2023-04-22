package zos.shell.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;

import java.util.ArrayList;
import java.util.List;

public class Member {

    private static final Logger LOG = LoggerFactory.getLogger(Member.class);

    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Member(ZosDsnList zosDsnList) {
        LOG.debug("*** Member ***");
        this.zosDsnList = zosDsnList;
    }

    public List<String> getMembers(String dataSet) throws Exception {
        LOG.debug("*** getMembers ***");
        List<zowe.client.sdk.zosfiles.response.Member> members = zosDsnList.listDsnMembers(dataSet, params);
        List<String> memberNames = new ArrayList<>();
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
