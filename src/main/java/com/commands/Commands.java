package com.commands;

import com.log.JobLog;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Commands {

    private final List<ZOSConnection> connections;
    private final TextTerminal<?> terminal;

    public Commands(List<ZOSConnection> connections, TextTerminal<?> terminal) {
        this.connections = connections;
        this.terminal = terminal;
    }

    public void cancel(ZOSConnection connection, String param) {
        var cancel = new Cancel(terminal, connection);
        cancel.cancel(param);
    }

    public void cat(ZOSConnection connection, String dataSet, String param) {
        var concatenate = new Concatenate(terminal, connection);
        concatenate.cat(dataSet, param);
    }

    public String cd(ZOSConnection connection, String currDataSet, String param) {
        var changeDir = new ChangeDir(terminal, connection);
        return changeDir.cd(currDataSet, param);
    }

    public ZOSConnection change(ZOSConnection connection, String[] commands) {
        var changeConn = new ChangeConn(terminal, connections);
        return changeConn.changeConnection(connection, commands);
    }

    public void connections(ZOSConnection connection) {
        var changeConn = new ChangeConn(terminal, connections);
        changeConn.displayConnections(connection);
    }

    public void copy(ZOSConnection connection, String currDataSet, String[] params) {
        var copy = new Copy(terminal, connection);
        copy.copy(currDataSet, params);
    }

    public void count(ZOSConnection connection, String dataSet, String param) {
        var count = new Count(terminal, connection);
        count.count(dataSet, param);
    }

    public void download(ZOSConnection currConnection, String currDataSet, String param) {
        var download = new Download(terminal, currConnection);
        download.download(currDataSet, param);
    }

    public void files() {
        LocalFiles.listFiles(terminal);
    }

    public JobLog get(ZOSConnection connection, String[] params) {
        return getAll(connection, params, false);
    }

    public JobLog getAll(ZOSConnection connection, String[] params, boolean isAll) {
        var getJobOutput = new GetJobOutput(terminal, connection, isAll);
        var output = getJobOutput.getLog(params[1]);
        if (output == null) return null;
        output.forEach(terminal::println);
        return new JobLog(params[1], output);
    }

    public void tail(ZOSConnection connection, String[] params) {
        tailAll(connection, params, false);
    }

    public void tailAll(ZOSConnection connection, String[] params, boolean isAll) {
        var getJobOutput = new GetJobOutput(terminal, connection, isAll);
        getJobOutput.tail(params);
    }

    public void touch(ZOSConnection connection, String currDataSet, String[] params) {
        var touch = new Touch(terminal, connection);
        touch.touch(currDataSet, params[1]);
    }

    public List<String> ls(ZOSConnection connection, String dataSet) {
        var listing = new Listing(connection, terminal);
        return listing.ls(dataSet, false);
    }

    public List<String> lsl(ZOSConnection connection, String dataSet) {
        var listing = new Listing(connection, terminal);
        return listing.ls(dataSet, true);
    }

    public void ps(ZOSConnection connection) {
        ps(connection, null);
    }

    public void ps(ZOSConnection connection, String task) {
        var processList = new ProcessList(terminal, connection);
        processList.ps(task);
    }

    public void rm(ZOSConnection connection, String currDataSet, String param) {
        var delete = new Delete(terminal, connection);
        delete.rm(currDataSet, param);
    }

    public void save(ZOSConnection connection, String currDataSet, String[] params) {
        var save = new Save(terminal, connection);
        save.save(currDataSet, params[1]);
    }

    public void search(JobLog jobLog, String text) {
        Optional<JobLog> log = Optional.ofNullable(jobLog);
        log.ifPresent((value) -> {
            var jobName = value.getJobName();
            var jobOutput = value.getOutput();
            terminal.println("searching " + jobName);
            List<String> results = jobOutput.stream().filter(line -> line.contains(text)).collect(Collectors.toList());
            if (!results.isEmpty())
                results.forEach(terminal::println);
            else terminal.println("no results found in job log for " + jobName);
        });
    }

    public void submit(ZOSConnection connection, String dataSet, String param) {
        var submit = new Submit(terminal, connection);
        submit.submitJob(dataSet, param);
    }

}
