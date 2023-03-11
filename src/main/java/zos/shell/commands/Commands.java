package zos.shell.commands;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import zos.shell.Constants;
import zos.shell.dto.Member;
import zos.shell.dto.Output;
import zos.shell.dto.ResponseStatus;
import zos.shell.future.*;
import zos.shell.utility.Help;
import zos.shell.utility.Util;
import zowe.client.sdk.core.SSHConnection;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosconsole.IssueCommand;
import zowe.client.sdk.zosfiles.ZosDsn;
import zowe.client.sdk.zosfiles.ZosDsnCopy;
import zowe.client.sdk.zosfiles.ZosDsnDownload;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.CreateParams;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.SubmitJobs;
import zowe.client.sdk.zosmfinfo.ListDefinedSystems;
import zowe.client.sdk.zosmfinfo.response.DefinedSystem;
import zowe.client.sdk.zosmfinfo.response.ZosmfListDefinedSystemsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Commands {

    private final List<ZOSConnection> connections;
    private final TextTerminal<?> terminal;
    private long timeOutValue = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(List<ZOSConnection> connections, TextTerminal<?> terminal) {
        this.connections = connections;
        this.terminal = terminal;
    }

    public Output browse(ZOSConnection connection, String[] params) {
        if (params.length == 3) {
            if (!"all".equalsIgnoreCase(params[2])) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
            return browseAll(connection, params, true);
        }
        return browseAll(connection, params, false);
    }

    private Output browseAll(ZOSConnection connection, String[] params, boolean isAll) {
        BrowseJob browseJob;
        try {
            browseJob = new BrowseJob(new GetJobs(connection), isAll, timeOutValue);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return null;
        }
        ResponseStatus responseStatus = browseJob.browseJob(params[1]);
        if (!responseStatus.isStatus() && responseStatus.getMessage().contains("timeout")) {
            terminal.println(Constants.BROWSE_TIMEOUT);
            return null;
        } else if (!responseStatus.isStatus()) {
            Util.printError(terminal, responseStatus.getMessage());
            return null;
        }
        String output = responseStatus.getMessage();
        terminal.println(output);
        return new Output(params[1], new StringBuilder(output));
    }

    public void cancel(ZOSConnection connection, String jobOrTask) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureTerminate(new IssueCommand(connection), Terminate.Type.CANCEL, jobOrTask));
        processFuture(pool, submit);
    }

    public StringBuilder cat(ZOSConnection connection, String dataSet, String member) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureConcatenate(new Download(new ZosDsnDownload(connection), false), dataSet, member));
        final var result = processFuture(pool, submit);
        if (result != null) {
            return new StringBuilder(result.getMessage());
        }
        return new StringBuilder();
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
        final var changeConn = new ChangeConn(terminal, connections);
        return changeConn.changeConnection(connection, commands);
    }

    public void color(String arg, String agr2) {
        final var color = new Color(terminal);
        color.setTextColor(arg);
        color.setBackGroundColor(agr2);
    }

    public void connections(ZOSConnection connection) {
        final var changeConn = new ChangeConn(terminal, connections);
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

        long count = params[1].chars().filter(ch -> ch == '*').count();
        if (count > 1) {
            terminal.println("invalid first argument, try again...");
            return;
        }

        if (params[1].contains("*") && Util.isMember(params[1].substring(0, params[1].indexOf("*")))) {
            var members = Util.getMembers(terminal, connection, currDataSet);
            final var index = params[1].indexOf("*");
            final var searchForMember = params[1].substring(0, index).toUpperCase();
            members = members.stream().filter(i -> i.startsWith(searchForMember)).collect(Collectors.toList());
            if (members.isEmpty()) {
                terminal.println(Constants.COPY_NOTHING_WARNING);
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
        terminal.println(copy.copy(currDataSet, params).getMessage());
    }

    public void copySequential(ZOSConnection connection, String currDataSet, String[] params) {
        CopySequential copy;
        try {
            copy = new CopySequential(new ZosDsnCopy(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        terminal.println(copy.copy(currDataSet, params).getMessage());
    }

    public void count(ZOSConnection connection, String dataSet, String filter) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureCount(new ZosDsnList(connection), dataSet, filter));
        processFuture(pool, submit);
    }

    public void download(ZOSConnection connection, String currDataSet, String member, boolean isBinary) {
        if ("*".equals(member)) {
            final List<String> members = Util.getMembers(terminal, connection, currDataSet);
            if (members.isEmpty()) {
                terminal.println(Constants.DOWNLOAD_NOTHING_WARNING);
                return;
            }
            multipleDownload(connection, currDataSet, members, isBinary).forEach(i -> terminal.println(i.getMessage()));
            return;
        }

        if (member.contains("*") && Util.isMember(member.substring(0, member.indexOf("*")))) {
            var members = Util.getMembers(terminal, connection, currDataSet);
            final var index = member.indexOf("*");
            final var searchForMember = member.substring(0, index).toUpperCase();
            members = members.stream().filter(i -> i.startsWith(searchForMember)).collect(Collectors.toList());
            if (members.isEmpty()) {
                terminal.println(Constants.DOWNLOAD_NOTHING_WARNING);
                return;
            }
            multipleDownload(connection, currDataSet, members, isBinary).forEach(i -> terminal.println(i.getMessage()));
            return;
        }

        Download download;
        try {
            download = new Download(new ZosDsnDownload(connection), isBinary);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        final var result = download.download(currDataSet, member);

        if (!result.isStatus()) {
            terminal.println(Util.getMsgAfterArrow(result.getMessage()));
            terminal.println("cannot open " + member + ", try again...");
        } else {
            terminal.println(result.getMessage());
            Util.openFileLocation(result.getOptionalData());
        }
    }

    public void downloadJob(ZOSConnection currConnection, String param, boolean isAll) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureDownloadJob(new GetJobs(currConnection), isAll, timeOutValue, param));
        processFuture(pool, submit);
    }

    private List<ResponseStatus> multipleDownload(ZOSConnection connection, String dataSet, List<String> members, boolean isBinary) {
        if (members.isEmpty()) {
            final var rs = new ResponseStatus("", false);
            final var rss = new ArrayList<ResponseStatus>();
            rss.add(rs);
            terminal.println(Constants.DOWNLOAD_NOTHING_WARNING);
            return rss;
        }
        final var pool = Executors.newFixedThreadPool(members.size());
        final var futures = new ArrayList<Future<ResponseStatus>>();

        for (final var member : members) {
            futures.add(pool.submit(new FutureDownload(new ZosDsnDownload(connection), dataSet, member, isBinary)));
        }

        final var result = getFutureResults(futures);
        Util.openFileLocation(result.get(0).getOptionalData());
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
        final var futures = new ArrayList<Future<ResponseStatus>>();

        for (final var member : members) {
            futures.add(pool.submit(new FutureCopy(new ZosDsnCopy(connection), fromDataSetName, toDataSetName, member)));
        }

        final var result = getFutureResults(futures);
        pool.shutdownNow();
        return result;
    }

    private List<ResponseStatus> getFutureResults(List<Future<ResponseStatus>> futures) {
        final var results = new ArrayList<ResponseStatus>();
        for (final var future : futures) {
            try {
                results.add(future.get(timeOutValue, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                results.add(new ResponseStatus("timeout", false));
            } catch (InterruptedException | ExecutionException e) {
                results.add(new ResponseStatus(Util.getErrorMsg(e + ""), false));
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

    public void ls(ZOSConnection connection, String member, String dataSet) {
        final var listing = new Listing(terminal, new ZosDsnList(connection), timeOutValue);
        try {
            listing.ls(member, dataSet, true, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
    }

    public void ls(ZOSConnection connection, String dataSet) {
        final var listing = new Listing(terminal, new ZosDsnList(connection), timeOutValue);
        try {
            listing.ls(null, dataSet, true, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
    }

    public void lsl(ZOSConnection connection, String dataSet, boolean isAttributes) {
        this.lsl(connection, null, dataSet, isAttributes);
    }

    public void lsl(ZOSConnection connection, String member, String dataSet, boolean isAttributes) {
        final var listing = new Listing(terminal, new ZosDsnList(connection), timeOutValue);
        try {
            listing.ls(member, dataSet, false, isAttributes);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
    }

    public void mkdir(ZOSConnection connection, TextIO mainTextIO, String currDataSet, String param) {
        final var createParamsBuilder = new CreateParams.Builder();

        var input = getMakeDirStr(mainTextIO,
                "Enter data set organization, PS (sequential), PO (partitioned), DA (direct), quit (to exit):");
        if (input == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.dsorg(input);

        var num = getMakeDirNum(mainTextIO, "Enter primary quantity number (enter quit to exit):");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.primary(num);

        num = getMakeDirNum(mainTextIO, "Enter secondary quantity number (enter quit to exit):");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.secondary(num);

        num = getMakeDirNum(mainTextIO, "Enter number of directory blocks (enter quit to exit):");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.dirblk(num);

        input = getMakeDirStr(mainTextIO, "Enter record format, FB, VB, U, etc (enter quit to exit):");
        if (input == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.recfm(input);

        num = getMakeDirNum(mainTextIO, "Enter block size number (enter quit to exit):");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.blksize(num);

        num = getMakeDirNum(mainTextIO, "Enter record length number (enter quit to exit):");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.lrecl(num);

        input = getMakeDirStr(mainTextIO, "Enter volume name (enter quit to skip):");
        if (!"quit".equalsIgnoreCase(input)) {
            createParamsBuilder.volser(input);
        }

        createParamsBuilder.alcunit("CYL");
        final var createParams = createParamsBuilder.build();
        MakeDirectory makeDirectory;
        try {
            makeDirectory = new MakeDirectory(new ZosDsn(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }

        if (!Util.isDataSet(param) && Util.isMember(param) && !currDataSet.isEmpty()) {
            param = currDataSet + "." + param;
        } else if (Util.isMember(param) && currDataSet.isEmpty()) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return;
        }

        param = param.toUpperCase();
        while (true) {
            input = getMakeDirStr(mainTextIO, "Create " + param + " (y/n)?:");
            if ("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input)) {
                break;
            }
            if ("n".equalsIgnoreCase(input) || "no".equalsIgnoreCase(input)) {
                terminal.println(Constants.MAKE_DIR_EXIT_MSG);
                return;
            }
        }

        terminal.println(makeDirectory.mkdir(param, createParams).getMessage());
    }

    private static Integer getMakeDirNum(TextIO mainTextIO, String prompt) {
        while (true) {
            final var input = mainTextIO.newStringInputReader().withMaxLength(80).read(prompt);
            if ("quit".equalsIgnoreCase(input)) {
                return null;
            }
            try {
                return (Integer.valueOf(input));
            } catch (Exception ignored) {
            }
        }
    }

    private static String getMakeDirStr(TextIO mainTextIO, String prompt) {
        String input;
        while (true) {
            input = mainTextIO.newStringInputReader().withMaxLength(80).read(prompt);
            if ("quit".equalsIgnoreCase(input)) {
                return null;
            }
            if (!Util.isStrNum(input)) {
                break;
            }
        }
        return input;
    }

    public void mvsCommand(ZOSConnection connection, String command) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureMvs(connection, command));
        processFuture(pool, submit);
    }

    public void ps(ZOSConnection connection) {
        ps(connection, null);
    }

    public void purgeJob(ZOSConnection connection, String item) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FuturePurgeJob(connection, item));
        processFuture(pool, submit);
    }

    public void ps(ZOSConnection connection, String jobOrTask) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureProcessList(new GetJobs(connection), jobOrTask));
        processFuture(pool, submit);
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

    public void save(ZOSConnection connection, String dataSet, String[] params) {
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureSave(new ZosDsn(connection), dataSet, member));
        processFuture(pool, submit);
    }

    public void search(Output output, String text) {
        final var search = new Search(terminal);
        search.search(output, text);
    }

    public void stop(ZOSConnection connection, String jobOrTask) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureTerminate(new IssueCommand(connection), Terminate.Type.STOP, jobOrTask));
        processFuture(pool, submit);
    }

    public void submit(ZOSConnection connection, String dataSet, String jobName) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureSubmit(new SubmitJobs(connection), dataSet, jobName));
        processFuture(pool, submit);
    }

    public Output tailJob(ZOSConnection connection, String[] params) {
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
            return new Output(params[1], tailAll(connection, params, true));
        }
        if (params.length == 3) {
            if ("all".equalsIgnoreCase(params[2])) {
                return new Output(params[1], tailAll(connection, params, true));
            }
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
        }
        return new Output(params[1], tailAll(connection, params, false));
    }

    private StringBuilder tailAll(ZOSConnection connection, String[] params, boolean isAll) {
        Tail tail;
        try {
            tail = new Tail(terminal, new GetJobs(connection), isAll, timeOutValue);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return null;
        }
        return tail.tail(params);
    }

    public void timeOutValue(long value) {
        timeOutValue = value;
        terminal.println("timeout value set to " + timeOutValue + " seconds.");
    }

    public void timeOutValue() {
        terminal.println("timeout value is " + timeOutValue + " seconds.");
    }

    public void touch(ZOSConnection connection, String dataSet, String[] params) {
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureTouch(new ZosDsn(connection), new Member(new ZosDsnList(connection)), dataSet, member));
        processFuture(pool, submit);
    }

    public void uname(ZOSConnection currConnection) {
        if (currConnection != null) {
            final var listDefinedSystems = new ListDefinedSystems(currConnection);
            ZosmfListDefinedSystemsResponse zosmfInfoResponse;
            Optional<String> osVersion = Optional.empty();
            Optional<String> sysName = Optional.empty();
            try {
                zosmfInfoResponse = listDefinedSystems.listDefinedSystems();
                DefinedSystem[] items;
                DefinedSystem item;
                if (zosmfInfoResponse.getDefinedSystems().isPresent()) {
                    items = zosmfInfoResponse.getDefinedSystems().get();
                    item = items[0];
                    osVersion = item.getZosVR();
                    sysName = item.getSystemName();
                }
            } catch (Exception ignored) {
            }
            terminal.println(
                    "hostname: " + currConnection.getHost() + ", OS: " + osVersion.orElse("n\\a") +
                            ", sysName: " + sysName.orElse("n\\a"));
        } else {
            terminal.println(Constants.NO_INFO);
        }
    }

    public void ussh(TextTerminal<?> terminal, ZOSConnection currConnection, Map<String, SSHConnection> sshConnections, String param) {
        final var uss = new Ussh(terminal, currConnection, sshConnections);
        uss.sshCommand(param);
    }

    public void vi(ZOSConnection connection, String dataSet, String[] params) {
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureVi(new Download(new ZosDsnDownload(connection), false), dataSet, member));
        processFuture(pool, submit);
    }

    private ResponseStatus processFuture(ExecutorService pool, Future<ResponseStatus> submit) {
        ResponseStatus result = null;
        try {
            result = submit.get(timeOutValue, TimeUnit.SECONDS);
            terminal.println(result.getMessage());
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
        pool.shutdownNow();
        return result;
    }

}
