package com.commands;

import com.Constants;
import com.data.JobOutput;
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

import java.util.List;

public class Commands {

    private final List<ZOSConnection> connections;
    private final TextTerminal<?> terminal;

    public Commands(List<ZOSConnection> connections, TextTerminal<?> terminal) {
        this.connections = connections;
        this.terminal = terminal;
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
            concatenate = new Concatenate(terminal, new Download(terminal, new ZosDsnDownload(connection)));
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

    public void download(ZOSConnection connection, String currDataSet, String param) {
        Download download;
        try {
            download = new Download(terminal, new ZosDsnDownload(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        download.download(currDataSet, param);
    }

    public void files() {
        LocalFiles.listFiles(terminal);
    }

    public JobOutput browse(ZOSConnection connection, String[] params) {
        return browseAll(connection, params, false);
    }

    public JobOutput browseAll(ZOSConnection connection, String[] params, boolean isAll) {
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
                terminal.println(Constants.GET_TIMEOUT_MSG);
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

    public List<String> ls(ZOSConnection connection, String dataSet) {
        var listing = new Listing(terminal, new ZosDsnList(connection));
        return listing.ls(dataSet, false);
    }

    public List<String> lsl(ZOSConnection connection, String dataSet) {
        var listing = new Listing(terminal, new ZosDsnList(connection));
        return listing.ls(dataSet, true);
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

    public void tail(ZOSConnection connection, String[] params) {
        tailAll(connection, params, false);
    }

    public void tailAll(ZOSConnection connection, String[] params, boolean isAll) {
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
        var vi = new Vi(terminal, new Download(terminal, new ZosDsnDownload(connection)));
        vi.vi(dataSet, params[1]);
    }

}
