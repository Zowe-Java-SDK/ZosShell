package com.future;

import com.commands.Download;
import com.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.ZosDsnDownload;

import java.util.concurrent.Callable;

public class FutureDownload extends Download implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureDownload(ZosDsnDownload download, String dataSet, String member) {
        super(download);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.futureDownload(dataSet, member);
    }

    public ResponseStatus futureDownload(String dataSet, String member) {
        return this.download(dataSet, member);
    }

}
