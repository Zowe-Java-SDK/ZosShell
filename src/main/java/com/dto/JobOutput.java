package com.dto;

import java.util.List;

public class JobOutput {

    private final String jobName;
    private final List<String> output;

    public JobOutput(String jobName, List<String> output) {
        this.jobName = jobName;
        this.output = output;
    }

    public String getJobName() {
        return jobName;
    }

    public List<String> getOutput() {
        return output;
    }

}
