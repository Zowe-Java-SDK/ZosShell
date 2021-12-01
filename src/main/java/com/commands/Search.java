package com.commands;

import com.log.JobLog;
import org.beryx.textio.TextTerminal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Search {

    private final TextTerminal<?> terminal;

    public Search(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void search(JobLog jobLog, String text) {
        Optional<JobLog> log = Optional.ofNullable(jobLog);
        log.ifPresentOrElse((value) -> {
            var jobName = value.getJobName();
            var jobOutput = value.getOutput();
            terminal.println("searching " + jobName);
            List<String> results = jobOutput.stream().filter(line -> line.contains(text)).collect(Collectors.toList());
            if (!results.isEmpty())
                results.forEach(terminal::println);
            else terminal.println("no results found in job log for " + jobName);
        }, () -> {
            terminal.println("nothing to search for, try again...");
        });
    }

}
