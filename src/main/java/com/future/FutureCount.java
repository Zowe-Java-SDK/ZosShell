package com.future;

import com.commands.Count;
import com.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.ZosDsnList;

import java.util.concurrent.Callable;

public class FutureCount extends Count implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String filter;

    public FutureCount(ZosDsnList zosDsnList, String dataSet, String filter) {
        super(zosDsnList);
        this.dataSet = dataSet;
        this.filter = filter;
    }

    @Override
    public ResponseStatus call() {
        return this.count(dataSet, filter);
    }

}
