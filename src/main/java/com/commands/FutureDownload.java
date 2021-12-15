package com.commands;

import com.dto.DownloadStatus;
import zosfiles.ZosDsnDownload;

import java.util.concurrent.Callable;

public class FutureDownload extends Download implements Callable<DownloadStatus> {

    private final String dataSet;
    private final String member;

    public FutureDownload(ZosDsnDownload download, String dataSet, String member) {
        super(download);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public DownloadStatus call() {
        return this.futureDownload(dataSet, member);
    }

    public DownloadStatus futureDownload(String dataSet, String member) {
        return this.download(dataSet, member);
    }

}
