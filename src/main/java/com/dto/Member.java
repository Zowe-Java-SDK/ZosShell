package com.dto;

import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;

import java.util.List;

public class Member {

    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Member(ZosDsnList zosDsnList) {
        this.zosDsnList = zosDsnList;
    }

    public List<String> getMembers(String dataSet) throws Exception {
        return zosDsnList.listDsnMembers(dataSet, params);
    }

    @Override
    public String toString() {
        return "Member{" +
                "zosDsnList=" + zosDsnList +
                ", params=" + params +
                '}';
    }
    
}
