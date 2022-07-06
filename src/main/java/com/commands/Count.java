package com.commands;

import com.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;
import zowe.client.sdk.zosfiles.response.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Count {

    private final ZosDsnList zosDsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Count(ZosDsnList zosDsnList) {
        this.zosDsnList = zosDsnList;
    }

    public ResponseStatus count(String dataSet, String param) {
        AtomicInteger dataSetCount = new AtomicInteger();
        List<Dataset> ds = new ArrayList<>();
        List<String> members = new ArrayList<>();
        try {
            if ("members".equalsIgnoreCase(param)) {
                members = zosDsnList.listDsnMembers(dataSet, params);
            }
            if ("datasets".equalsIgnoreCase(param)) {
                ds = zosDsnList.listDsn(dataSet, params);
            }
            ds.forEach(item -> {
                if (!item.getDsname().orElse("n\\a").equalsIgnoreCase(dataSet)) {
                    dataSetCount.getAndIncrement();
                }
            });
        } catch (Exception e) {
            return new ResponseStatus("0", false);
        }
        return new ResponseStatus(String.valueOf(members.size() + dataSetCount.get()), true);
    }

}
