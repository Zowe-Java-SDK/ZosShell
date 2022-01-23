package com.commands;

import com.Constants;
import com.dto.JobOutput;
import com.dto.ResponseStatus;
import com.future.FutureCopy;
import com.future.FutureDownload;
import com.utility.Help;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosconsole.IssueCommand;
import zowe.client.sdk.zosfiles.ZosDsn;
import zowe.client.sdk.zosfiles.ZosDsnCopy;
import zowe.client.sdk.zosfiles.ZosDsnDownload;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.SubmitJobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Commands {

    private final List<ZOSConnection> connections;
    private final TextTerminal<?> terminal;

    public Commands(List<ZOSConnection> connections, TextTerminal<?> terminal) {
        this.connections = connections;
        this.terminal = terminal;
    }

    public JobOutput browse(ZOSConnection connection, String[] params) {
        if (params.length == 3) {
            if (!"all".equalsIgnoreCase(params[2])) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
            return browseAll(connection, params, true);
        }
        return browseAll(connection, params, false);
    }

    private JobOutput browseAll(ZOSConnection connection, String[] params, boolean isAll) {
        BrowseJob browseJob;
        try {
            browseJob = new BrowseJob(terminal, new GetJobs(connection), isAll);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return null;
        }
        StringBuilder output = new StringBuilder();
        try {
            output = browseJob.browseJob(params[1]);
        } catch (Exception e) {
            if (e.getMessage().contains("timeout")) {
                terminal.println(Constants.BROWSE_TIMEOUT_MSG);
                return null;
            }
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return null;
            }
            Util.printError(terminal, e.getMessage());
            return null;
        }
        terminal.println(output.toString());
        return new JobOutput(params[1], output);
    }

    public void cancel(ZOSConnection connection, String param) {
        Cancel cancel;
        try {
            cancel = new Cancel(terminal, new IssueCommand(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        cancel.cancel(param);
    }

    public void cat(ZOSConnection connection, String dataSet, String param) {
        Concatenate concatenate;
        try {
            concatenate = new Concatenate(terminal, new Download(new ZosDsnDownload(connection)));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        concatenate.cat(dataSet, param);
    }

    public String cd(ZOSConnection connection, String currDataSet, String param) {
        ChangeDir changeDir;
        try {
            changeDir = new ChangeDir(terminal, new ZosDsnList(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return currDataSet;
        }
        return changeDir.cd(currDataSet, param);
    }

    public ZOSConnection change(ZOSConnection connection, String[] commands) {
        var changeConn = new ChangeConn(terminal, connections);
        return changeConn.changeConnection(connection, commands);
    }

    public void color(String param) {
        var color = new Color(terminal);
        color.color(param);
    }

    public void connections(ZOSConnection connection) {
        var changeConn = new ChangeConn(terminal, connections);
        changeConn.displayConnections(connection);
    }

    public void copy(ZOSConnection connection, String currDataSet, String[] params) {
        if ("*".equals(params[1])) {
            final List<String> members = Util.getMembers(terminal, connection, currDataSet);
            if (members.isEmpty()) {
                return;
            }
            multipleCopy(connection, currDataSet, params[2], members).forEach(i -> terminal.println(i.getMessage()));
            return;
        }
        Copy copy;
        try {
            copy = new Copy(new ZosDsnCopy(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        var responseStatus = copy.copy(currDataSet, params);
        terminal.println(responseStatus.getMessage());
    }

    public void count(ZOSConnection connection, String dataSet, String param) {
        Count count;
        try {
            count = new Count(terminal, new ZosDsnList(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        count.count(dataSet, param);
    }

    public void download(ZOSConnection connection, String currDataSet, String member) {
        if ("*".equals(member)) {
            final List<String> members = Util.getMembers(terminal, connection, currDataSet);
            if (members.isEmpty()) {
                return;
            }
            multipleDownload(connection, currDataSet, members).forEach(i -> terminal.println(i.getMessage()));
            return;
        }
        Download download;
        try {
            download = new Download(new ZosDsnDownload(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        var result = download.download(currDataSet, member);

        if (!result.isStatus()) {
            terminal.println(Util.getMsgAfterArrow(result.getMessage()));
            terminal.println("cannot open " + member + ", try again...");
        } else {
            terminal.println(result.getMessage());
        }
    }

    private List<ResponseStatus> multipleDownload(ZOSConnection connection, String dataSet, List<String> members) {
        final var pool = Executors.newFixedThreadPool(members.size());
        var futures = new ArrayList<Future<ResponseStatus>>();

        for (var member : members) {
            futures.add(pool.submit(new FutureDownload(new ZosDsnDownload(connection), dataSet, member)));
        }

        var result = getFutureResults(futures);
        pool.shutdownNow();
        return result;
    }

    private List<ResponseStatus> multipleCopy(ZOSConnection connection, String fromDataSetName, String toDataSetName,
                                              List<String> members) {
        if (!Util.isDataSet(toDataSetName)) {
            terminal.println(Constants.INVALID_DATASET);
            return new ArrayList<>();
        }

        final var pool = Executors.newFixedThreadPool(members.size());
        var futures = new ArrayList<Future<ResponseStatus>>();

        for (var member : members) {
            futures.add(pool.submit(new FutureCopy(new ZosDsnCopy(connection), fromDataSetName, toDataSetName, member)));
        }

        var result = getFutureResults(futures);
        pool.shutdownNow();
        return result;
    }

    private List<ResponseStatus> getFutureResults(List<Future<ResponseStatus>> futures) {
        var results = new ArrayList<ResponseStatus>();
        for (var future : futures) {
            try {
                results.add(future.get(Constants.FUTURE_TIMEOUT_VALUE, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                results.add(new ResponseStatus("timeout", false));
            }
        }
        return results;
    }

    public void files(String dataSet) {
        LocalFiles.listFiles(terminal, dataSet);
    }

    public void help() {
        Help.displayHelp(terminal);
    }

    public void ls(ZOSConnection connection, String dataSet) {
        var listing = new Listing(terminal, new ZosDsnList(connection));
        listing.ls(dataSet, false);
    }

    public void lsl(ZOSConnection connection, String dataSet) {
        var listing = new Listing(terminal, new ZosDsnList(connection));
        listing.ls(dataSet, true);
    }

    public void ps(ZOSConnection connection) {
        ps(connection, null);
    }

    public void ps(ZOSConnection connection, String task) {
        ProcessList processList;
        try {
            processList = new ProcessList(terminal, new GetJobs(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        processList.ps(task);
    }

    public void rm(ZOSConnection connection, String currDataSet, String param) {
        Delete delete;
        try {
            delete = new Delete(terminal, new ZosDsn(connection), new ZosDsnList(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        delete.rm(currDataSet, param);
    }

    public void save(ZOSConnection connection, String currDataSet, String[] params) {
        Save save;
        try {
            save = new Save(terminal, new ZosDsn(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        save.save(currDataSet, params[1]);
    }

    public void search(JobOutput job, String text) {
        var search = new Search(terminal);
        search.search(job, text);
    }

    public void stop(ZOSConnection connection, String param) {
        Stop stop;
        try {
            stop = new Stop(terminal, new IssueCommand(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        stop.stop(param);
    }

    public void submit(ZOSConnection connection, String dataSet, String param) {
        var submit = new Submit(terminal, new SubmitJobs(connection));
        submit.submitJob(dataSet, param);
    }

    public JobOutput tailjob(ZOSConnection connection, String[] params) {
        if (params.length == 4) {
            if (!"all".equalsIgnoreCase(params[3])) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
            return new JobOutput(params[1], tailAll(connection, params, true));
        }
        if (params.length == 3) {
            if ("all".equalsIgnoreCase(params[2])) {
                return new JobOutput(params[1], tailAll(connection, params, true));
            }
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
        }
        return new JobOutput(params[1], tailAll(connection, params, false));
    }

    private StringBuilder tailAll(ZOSConnection connection, String[] params, boolean isAll) {
        Tail tail;
        try {
            tail = new Tail(terminal, new GetJobs(connection), isAll);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return null;
        }
        return tail.tail(params);
    }

    public void touch(ZOSConnection connection, String currDataSet, String[] params) {
        Touch touch;
        try {
            touch = new Touch(terminal, new ZosDsn(connection), new Listing(terminal, new ZosDsnList(connection)));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        touch.touch(currDataSet, params[1]);
    }

    public void vi(ZOSConnection connection, String dataSet, String[] params) {
        var vi = new Vi(terminal, new Download(new ZosDsnDownload(connection)));
        vi.vi(dataSet, params[1]);
    }

}
