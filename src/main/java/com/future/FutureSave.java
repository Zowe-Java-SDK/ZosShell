package com.future;

import com.commands.Save;
import com.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.ZosDsn;

import java.util.concurrent.Callable;

public class FutureSave extends Save implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureSave(ZosDsn zosDsn, String dataSet, String member) {
        super(zosDsn);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.save(dataSet, member);
    }

}
