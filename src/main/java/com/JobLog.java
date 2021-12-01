package com;

public class JobLog {

    private String jobName;
    private String[] output;

    public JobLog(String jobName, String[] output) {
        this.jobName = jobName;
        this.output = output;
    }

    public String getJobName() {
        return jobName;
    }

    public String[] getOutput() {
        return output;
    }

}
