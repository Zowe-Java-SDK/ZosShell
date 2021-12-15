package com.commands;

import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsnDownload;

import java.util.concurrent.Callable;

public class FutureDownload extends Download implements Callable<DownloadStatus> {

    private String dataSet;
    private String member;

    public FutureDownload(TextTerminal<?> terminal, ZosDsnDownload download, String dataSet, String member) {
        super(terminal, download);
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
