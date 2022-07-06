package com.future;

import com.commands.Download;
import com.commands.Vi;
import com.dto.ResponseStatus;

import java.util.concurrent.Callable;

public class FutureVi extends Vi implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureVi(Download download, String dataSet, String member) {
        super(download);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.vi(this.dataSet, this.member);
    }

}
