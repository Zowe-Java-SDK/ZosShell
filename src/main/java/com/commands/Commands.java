package com.commands;

import com.Constants;
import com.dto.DownloadStatus;
import com.dto.JobOutput;
import com.utility.Help;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosconsole.IssueCommand;
import zosfiles.ZosDsn;
import zosfiles.ZosDsnCopy;
import zosfiles.ZosDsnDownload;
import zosfiles.ZosDsnList;
import zosjobs.GetJobs;
import zosjobs.SubmitJobs;

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
        List<String> output;
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
        output.forEach(terminal::println);
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

    public void connections(ZOSConnection connection) {
        var changeConn = new ChangeConn(terminal, connections);
        changeConn.displayConnections(connection);
    }

    public void copy(ZOSConnection connection, String currDataSet, String[] params) {
        Copy copy;
        try {
            copy = new Copy(terminal, new ZosDsnCopy(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        copy.copy(currDataSet, params);
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
            var listing = new Listing(terminal, new ZosDsnList(connection));
            final List<String> members;
            try {
                members = listing.getMembers(currDataSet);
            } catch (Exception e) {
                Util.printError(terminal, e.getMessage());
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
        DownloadStatus result = download.download(currDataSet, member);

        if (!result.isStatus()) {
            terminal.println(Util.getMsgAfterArrow(result.getMessage()));
            terminal.println("cannot open " + member + ", try again...");
        } else {
            terminal.println(result.getMessage());
        }
    }

    private List<DownloadStatus> multipleDownload(ZOSConnection connection, String dataSet, List<String> members) {
        int size = members.size();
        final var pool = Executors.newFixedThreadPool(members.size());
        List<DownloadStatus> results = new ArrayList<>();
        List<Future<DownloadStatus>> futures = new ArrayList<>();

        for (String member : members) {
            futures.add(pool.submit(
                    new FutureDownload(new ZosDsnDownload(connection), dataSet, member)));
        }

        for (int i = 0; i < size; i++) {
            try {
                results.add(futures.get(i).get(10, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                results.add(new DownloadStatus("timeout", false));
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

    public void tailjob(ZOSConnection connection, String[] params) {
        if (params.length == 4) {
            if (!"all".equalsIgnoreCase(params[3])) {
                terminal.println(Constants.INVALID_PARAMETER);
                return;
            }
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return;
            }
            tailAll(connection, params, true);
            return;
        }
        if (params.length == 3) {
            if ("all".equalsIgnoreCase(params[2])) {
                tailAll(connection, params, true);
                return;
            }
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return;
            }
        }
        tailAll(connection, params, false);
    }

    private void tailAll(ZOSConnection connection, String[] params, boolean isAll) {
        Tail tail;
        try {
            tail = new Tail(terminal, new GetJobs(connection), isAll);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        tail.tail(params);
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
