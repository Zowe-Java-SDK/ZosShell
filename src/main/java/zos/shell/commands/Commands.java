package zos.shell.commands;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.data.Environment;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Commands {

    private static final Logger LOG = LoggerFactory.getLogger(Commands.class);

    private final List<ZOSConnection> connections;
    private final TextTerminal<?> terminal;
    private long timeOutValue = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(List<ZOSConnection> connections, TextTerminal<?> terminal) {
        LOG.debug("*** Commands ***");
        this.connections = connections;
        this.terminal = terminal;
    }

    public Output browse(ZOSConnection connection, String[] params) {
        LOG.debug("*** browse ***");
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
        LOG.debug("*** browseAll ***");
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
        LOG.debug("*** cancel ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTerminate(new IssueCommand(connection), Terminate.Type.CANCEL, jobOrTask));
        processFuture(pool, submit);
    }

    public Output cat(ZOSConnection connection, String dataSet, String member) {
        LOG.debug("*** cat ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(
                new FutureConcatenate(new Download(new ZosDsnDownload(connection), false), dataSet, member));
        final var result = processFuture(pool, submit);
        if (result.isStatus()) {
            return new Output("cat", new StringBuilder(result.getMessage()));
        }
        return new Output("cat", new StringBuilder());
    }

    public String cd(ZOSConnection connection, String currDataSet, String param) {
        LOG.debug("*** cd ***");
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
        LOG.debug("*** change ***");
        final var changeConn = new ChangeConn(terminal, connections);
        return changeConn.changeConnection(connection, commands);
    }

    public void color(String arg, String agr2) {
        LOG.debug("*** color ***");
        final var color = new Color(terminal);
        color.setTextColor(arg);
        color.setBackGroundColor(agr2);
    }

    public void connections(ZOSConnection connection) {
        LOG.debug("*** connections ***");
        final var changeConn = new ChangeConn(terminal, connections);
        changeConn.displayConnections(connection);
    }

    public void copy(ZOSConnection connection, String currDataSet, String[] params) {
        LOG.debug("*** copy ***");
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

    private List<ResponseStatus> multipleCopy(ZOSConnection connection, String fromDataSetName, String toDataSetName,
                                              List<String> members) {
        LOG.debug("*** multipleCopy ***");
        if (!Util.isDataSet(toDataSetName)) {
            terminal.println(Constants.INVALID_DATASET);
            return new ArrayList<>();
        }

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        final var futures = new ArrayList<Future<ResponseStatus>>();

        for (final var member : members) {
            futures.add(pool.submit(new FutureCopy(new ZosDsnCopy(connection), fromDataSetName, toDataSetName, member)));
        }

        final var result = getFutureResults(futures);
        pool.shutdownNow();
        return result;
    }

    public void copySequential(ZOSConnection connection, String currDataSet, String[] params) {
        LOG.debug("*** copySequential ***");
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
        LOG.debug("*** count ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureCount(new ZosDsnList(connection), dataSet, filter));
        processFuture(pool, submit);
    }

    public void download(ZOSConnection connection, String currDataSet, String member, boolean isBinary) {
        LOG.debug("*** download ***");
        if ("*".equals(member)) {
            final var members = Util.getMembers(terminal, connection, currDataSet);
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
        LOG.debug("*** downloadJob ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureDownloadJob(new GetJobs(currConnection), isAll, timeOutValue, param));
        processFuture(pool, submit);
    }

    private List<ResponseStatus> multipleDownload(ZOSConnection connection, String dataSet, List<String> members,
                                                  boolean isBinary) {
        LOG.debug("*** multipleDownload ***");
        if (members.isEmpty()) {
            final var rs = new ResponseStatus("", false);
            final var rss = new ArrayList<ResponseStatus>();
            rss.add(rs);
            terminal.println(Constants.DOWNLOAD_NOTHING_WARNING);
            return rss;
        }
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        final var futures = new ArrayList<Future<ResponseStatus>>();

        for (final var member : members) {
            futures.add(pool.submit(new FutureDownload(new ZosDsnDownload(connection), dataSet, member, isBinary)));
        }

        final var result = getFutureResults(futures);
        Util.openFileLocation(result.get(0).getOptionalData());
        pool.shutdownNow();
        return result;
    }

    private List<ResponseStatus> getFutureResults(List<Future<ResponseStatus>> futures) {
        LOG.debug("*** getFutureResults ***");
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

    public Output env() {
        LOG.debug("*** env ***");
        final var env = Environment.getInstance();
        if (env.getVariables().isEmpty()) {
            terminal.println("no environment variables set, try again...");
        }
        final var str = new StringBuilder();
        new TreeMap<>(env.getVariables()).forEach((k, v) -> {
            final var value = k + "=" + v;
            str.append(value).append("\n");
            terminal.println(value);
        });
        return new Output("env", str);
    }

    public void files(String dataSet) {
        LOG.debug("*** files ***");
        LocalFiles.listFiles(terminal, dataSet);
    }

    public void grep(ZOSConnection connection, String pattern, String member, String dataSet) {
        LOG.debug("*** grep ***");
        if ("*".equals(member)) {
            final var members = Util.getMembers(terminal, connection, dataSet);
            multipleGrep(connection, pattern, dataSet, members).forEach(terminal::println);
            return;
        }

        if (member.contains("*") && Util.isMember(member.substring(0, member.indexOf("*")))) {
            var members = Util.getMembers(terminal, connection, dataSet);
            final var searchForMember = member.substring(0, member.indexOf("*")).toUpperCase();
            members = members.stream().filter(i -> i.startsWith(searchForMember)).collect(Collectors.toList());
            multipleGrep(connection, pattern, dataSet, members).forEach(terminal::println);
            return;
        }

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var concatenate = new Concatenate(new Download(new ZosDsnDownload(connection), false));
        final var submit = pool.submit(new FutureGrep(new Grep(concatenate, pattern), dataSet, member));
        List<String> result;
        try {
            result = submit.get(timeOutValue, TimeUnit.SECONDS);
            result.forEach(terminal::println);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
        pool.shutdownNow();
    }

    private List<String> multipleGrep(ZOSConnection connection, String pattern, String dataSet, List<String> members) {
        LOG.debug("*** multipleGrep ***");
        final var results = new ArrayList<String>();
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        final var futures = new ArrayList<Future<List<String>>>();

        for (final var member : members) {
            final var concatenate = new Concatenate(new Download(new ZosDsnDownload(connection), false));
            futures.add(pool.submit(new FutureGrep(new Grep(concatenate, pattern, true), dataSet, member)));
        }

        for (final var future : futures) {
            try {
                results.addAll(future.get(timeOutValue, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                results.add("timeout");
            } catch (InterruptedException | ExecutionException e) {
                results.add(Util.getErrorMsg(e + ""));
            }
        }

        pool.shutdownNow();
        if (results.isEmpty()) {
            results.add("nothing found, try again...");
        }
        return results;
    }

    public Output help() {
        LOG.debug("*** help ***");
        return Help.displayHelp(terminal);
    }

    public void ls(ZOSConnection connection, String member, String dataSet) {
        LOG.debug("*** ls 1 ***");
        final var listing = new Listing(terminal, new ZosDsnList(connection), timeOutValue);
        try {
            listing.ls(member, dataSet, true, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void ls(ZOSConnection connection, String dataSet) {
        LOG.debug("*** ls 2 ***");
        final var listing = new Listing(terminal, new ZosDsnList(connection), timeOutValue);
        try {
            listing.ls(null, dataSet, true, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void lsl(ZOSConnection connection, String dataSet, boolean isAttributes) {
        LOG.debug("*** lsl 1 ***");
        this.lsl(connection, null, dataSet, isAttributes);
    }

    public void lsl(ZOSConnection connection, String member, String dataSet, boolean isAttributes) {
        LOG.debug("*** lsl 2 ***");
        final var listing = new Listing(terminal, new ZosDsnList(connection), timeOutValue);
        try {
            listing.ls(member, dataSet, false, isAttributes);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void mkdir(ZOSConnection connection, TextIO mainTextIO, String currDataSet, String param) {
        LOG.debug("*** mkdir ***");
        if (param.contains(".") && !Util.isDataSet(param)) {
            terminal.println("invalid data set character sequence, try again...");
            return;
        }
        if (!param.contains(".") && !Util.isMember(param)) {
            terminal.println("invalid 8 character sequence, try again...");
            return;
        }

        if (!Util.isDataSet(param) && Util.isMember(param) && !currDataSet.isEmpty()) {
            param = currDataSet + "." + param;
        } else if (Util.isMember(param) && currDataSet.isEmpty()) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return;
        }

        final var createParamsBuilder = new CreateParams.Builder();
        var isSequential = false;

        terminal.println("To quit, enter 'q', 'quit' or 'exit' at any prompt.");
        String input;
        while (true) {
            input = getMakeDirStr(mainTextIO,
                    "Enter data set organization, PS (sequential), PO (partitioned), DA (direct):");
            if (input == null) {
                terminal.println(Constants.MAKE_DIR_EXIT_MSG);
                return;
            }
            if ("PS".equalsIgnoreCase(input) || "PO".equalsIgnoreCase(input) || "DA".equalsIgnoreCase(input)) {
                if ("PS".equalsIgnoreCase(input)) {
                    isSequential = true;
                }
                break;
            }
        }
        createParamsBuilder.dsorg(input);

        var num = getMakeDirNum(mainTextIO, "Enter primary quantity number:");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.primary(num);

        num = getMakeDirNum(mainTextIO, "Enter secondary quantity number:");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.secondary(num);

        if (!isSequential) {
            num = getMakeDirNum(mainTextIO, "Enter number of directory blocks:");
            if (num == null) {
                terminal.println(Constants.MAKE_DIR_EXIT_MSG);
                return;
            }
            createParamsBuilder.dirblk(num);
        } else {
            createParamsBuilder.dirblk(0);
        }

        input = getMakeDirStr(mainTextIO, "Enter record format, FB, VB, U, etc:");
        if (input == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.recfm(input);

        num = getMakeDirNum(mainTextIO, "Enter block size number:");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.blksize(num);

        num = getMakeDirNum(mainTextIO, "Enter record length number:");
        if (num == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        createParamsBuilder.lrecl(num);

        input = getMakeDirStr(mainTextIO, "Enter volume name ('s' to skip):");
        if (input == null) {
            terminal.println(Constants.MAKE_DIR_EXIT_MSG);
            return;
        }
        if (!"s".equalsIgnoreCase(input) && !"skip".equalsIgnoreCase(input)) {
            createParamsBuilder.volser(input);
        }

        createParamsBuilder.alcunit("CYL");
        final var createParams = createParamsBuilder.build();

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

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureMakeDirectory(new ZosDsn(connection), param, createParams));
        processFuture(pool, submit);
    }

    private static Integer getMakeDirNum(TextIO mainTextIO, String prompt) {
        LOG.debug("*** getMakeDirNum ***");
        while (true) {
            final var input = mainTextIO.newStringInputReader().withMaxLength(80).read(prompt);
            if ("q".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input)) {
                return null;
            }
            try {
                return (Integer.valueOf(input));
            } catch (Exception ignored) {
            }
        }
    }

    private static String getMakeDirStr(TextIO mainTextIO, String prompt) {
        LOG.debug("*** getMakeDirStr ***");
        String input;
        do {
            input = mainTextIO.newStringInputReader().withMaxLength(80).read(prompt);
            if ("q".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input)) {
                return null;
            }
        } while (Util.isStrNum(input));
        return input;
    }

    public Output mvsCommand(ZOSConnection connection, String command) {
        LOG.debug("*** mvsCommand ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureMvs(connection, command));
        final var response = processFuture(pool, submit);
        return response != null && response.isStatus() ? new Output("mvs",
                new StringBuilder(response.getMessage())) : null;
    }

    public Output ps(ZOSConnection connection) {
        LOG.debug("*** ps ***");
        return ps(connection, null);
    }

    public void purgeJob(ZOSConnection connection, String item) {
        LOG.debug("*** purgeJob ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FuturePurgeJob(connection, item));
        processFuture(pool, submit);
    }

    public Output ps(ZOSConnection connection, String jobOrTask) {
        LOG.debug("*** ps ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureProcessList(new GetJobs(connection), jobOrTask));
        final var response = processFuture(pool, submit);
        return response != null && response.isStatus() ? new Output("ps",
                new StringBuilder(response.getMessage())) : null;
    }

    public void rm(ZOSConnection connection, String currDataSet, String param) {
        LOG.debug("*** rm ***");
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
        LOG.debug("*** save ***");
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureSave(new ZosDsn(connection), dataSet, member));
        processFuture(pool, submit);
    }

    public void search(Output output, String text) {
        LOG.debug("*** search ***");
        final var search = new Search(terminal);
        search.search(output, text);
    }

    public void set(final String param) {
        LOG.debug("*** set ***");
        final var values = param.split("=");
        if (values.length != 2) {
            terminal.println(Constants.INVALID_COMMAND);
            return;
        }
        Environment.getInstance().setVariable(values[0], values[1]);
        terminal.println(values[0] + "=" + values[1]);
    }

    public void stop(ZOSConnection connection, String jobOrTask) {
        LOG.debug("*** stop ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTerminate(new IssueCommand(connection), Terminate.Type.STOP,
                jobOrTask));
        processFuture(pool, submit);
    }

    public void submit(ZOSConnection connection, String dataSet, String jobName) {
        LOG.debug("*** submit ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureSubmit(new SubmitJobs(connection), dataSet, jobName));
        processFuture(pool, submit);
    }

    public Output tailJob(ZOSConnection connection, String[] params) {
        LOG.debug("*** tailJob ***");
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
            return processTailJobResponse(connection, params, true);
        }
        if (params.length == 3) {
            if ("all".equalsIgnoreCase(params[2])) {
                return processTailJobResponse(connection, params, true);
            }
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
        }
        return processTailJobResponse(connection, params, false);
    }

    private Output processTailJobResponse(ZOSConnection connection, String[] params, boolean isAll) {
        final var response = tailAll(connection, params, isAll);
        if (response != null && !response.isStatus()) { // false nothing displayed println error message
            terminal.println(response.getMessage());
        }
        return response != null && response.isStatus() ? new Output(params[1],
                new StringBuilder(response.getMessage())) : null;
    }

    private ResponseStatus tailAll(ZOSConnection connection, String[] params, boolean isAll) {
        LOG.debug("*** tailAll ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTailJob(terminal, connection, isAll, timeOutValue, params));
        return processFuture(pool, submit);
    }

    public void timeOutValue(long value) {
        LOG.debug("*** timeOutValue ***");
        timeOutValue = value;
        terminal.println("timeout value set to " + timeOutValue + " seconds.");
    }

    public void timeOutValue() {
        LOG.debug("*** timeOutValue ***");
        terminal.println("timeout value is " + timeOutValue + " seconds.");
    }

    public void touch(ZOSConnection connection, String dataSet, String[] params) {
        LOG.debug("*** touch ***");
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTouch(new ZosDsn(connection),
                new Member(new ZosDsnList(connection)), dataSet, member));
        processFuture(pool, submit);
    }

    public Output tsoCommand(ZOSConnection connection, String accountNumber, String command) {
        LOG.debug("*** tsoCommand ***");
        if (accountNumber == null) {
            terminal.println("ACCTNUM is not set, try again...");
            return new Output("tso", new StringBuilder());
        }
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTso(connection, accountNumber, command));
        final var response = processFuture(pool, submit);
        return response != null && response.isStatus() ? new Output("tso",
                new StringBuilder(response.getMessage())) : null;
    }

    public void uname(ZOSConnection currConnection) {
        LOG.debug("*** uname ***");
        if (currConnection != null) {
            Optional<String> zosVersion = Optional.empty();
            try {
                final var issueCommand = new IssueCommand(currConnection);
                final var response = issueCommand.issueSimple("D IPLINFO");
                final var output = response.getCommandResponse()
                        .orElseThrow((() -> new Exception("IPLINFO command no response")));
                final var index = output.indexOf("RELEASE z/OS ");
                if (index >= 0) {
                    zosVersion = Optional.of(output.substring(index, index + 22));
                }
            } catch (Exception e) {
                LOG.debug(e.getMessage());
            }
            terminal.println(
                    "hostname: " + currConnection.getHost() + ", " + zosVersion.orElse("n\\a"));
        } else {
            terminal.println(Constants.NO_INFO);
        }
    }

    public void ussh(TextTerminal<?> terminal, ZOSConnection currConnection,
                     Map<String, SSHConnection> sshConnections, String param) {
        LOG.debug("*** ussh ***");
        final var uss = new Ussh(terminal, currConnection, sshConnections);
        uss.sshCommand(param);
    }

    public void vi(ZOSConnection connection, String dataSet, String[] params) {
        LOG.debug("*** vi ***");
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureVi(
                new Download(new ZosDsnDownload(connection), false), dataSet, member));
        processFuture(pool, submit);
    }

    private ResponseStatus processFuture(ExecutorService pool, Future<ResponseStatus> submit) {
        LOG.debug("*** processFuture ***");
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
