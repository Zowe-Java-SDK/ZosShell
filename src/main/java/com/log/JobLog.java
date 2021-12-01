package com.log;

import java.util.List;

public class JobLog {

    private String jobName;
    private List<String> output;

    public JobLog(String jobName, List<String> output) {
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
