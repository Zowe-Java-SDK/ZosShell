package com.future;

import com.commands.Touch;
import com.dto.Member;
import com.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.ZosDsn;

import java.util.concurrent.Callable;

public class FutureTouch extends Touch implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureTouch(ZosDsn zosDsn, Member members, String dataSet, String member) {
        super(zosDsn, members);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.touch(this.dataSet, this.member);
    }

}
