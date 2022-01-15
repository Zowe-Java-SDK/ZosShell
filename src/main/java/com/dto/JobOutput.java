package com.dto;

public class JobOutput {

    private final String jobName;
    private final StringBuilder output;

    public JobOutput(String jobName, StringBuilder output) {
        this.jobName = jobName;
        this.output = output;
    }

    public String getJobName() {
        return jobName;
    }

    public StringBuilder getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "JobOutput{" +
                "jobName='" + jobName + '\'' + '}';
    }

}
