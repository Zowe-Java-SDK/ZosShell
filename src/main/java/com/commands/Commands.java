package com.commands;

import core.ZOSConnection;
import org.beryx.textio.TextTerminal;

import java.util.Arrays;
import java.util.List;

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

    public void get(ZOSConnection connection, String[] params) {
        var getJobOutput = new GetJobOutput(terminal, connection);
        var output = getJobOutput.getLog(params[1]);
        if (output == null) return;
        Arrays.stream(output).forEach(terminal::println);
    }

    public void tail(ZOSConnection connection, String[] params) {
        var getJobOutput = new GetJobOutput(terminal, connection);
        getJobOutput.tail(params);
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

    public void submit(ZOSConnection connection, String dataSet, String param) {
        var submit = new Submit(terminal, connection);
        submit.submitJob(dataSet, param);
    }

}
