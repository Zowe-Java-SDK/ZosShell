package com.future;

import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.input.JobFile;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureBrowseJob implements Callable<StringBuilder> {

    private final GetJobs getJobs;
    private final List<JobFile> files;

    public FutureBrowseJob(GetJobs getJobs, List<JobFile> files) {
        this.getJobs = getJobs;
        this.files = files;
    }

    @Override
    public StringBuilder call() {
        final var str = new StringBuilder();
        files.forEach(file -> {
            try {
                str.append(List.of(getJobs.getSpoolContent(file)));
            } catch (Exception ignored) {
            }
        });
        return str;
    }

}
