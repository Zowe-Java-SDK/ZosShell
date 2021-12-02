package com.commands;

import zosjobs.GetJobs;
import zosjobs.input.JobFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class GetAllJobLog implements Callable<List<String>> {

    private final GetJobs getJobs;
    private final List<JobFile> files;

    public GetAllJobLog(GetJobs getJobs, List<JobFile> files) {
        this.getJobs = getJobs;
        this.files = files;
    }

    @Override
    public List<String> call() {
        List<String> results = new ArrayList<>();
        files.forEach(file -> {
            try {
                results.addAll(Arrays.asList(getJobs.getSpoolContent(file).split("\n")));
            } catch (Exception ignored) {
            }
        });
        return results;
    }

}
