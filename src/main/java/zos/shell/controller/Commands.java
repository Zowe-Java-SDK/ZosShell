package zos.shell.controller;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.dto.Member;
import zos.shell.dto.Output;
import zos.shell.future.*;
import zos.shell.response.ResponseStatus;
import zos.shell.service.change.ColorCmd;
import zos.shell.service.change.ConnCmd;
import zos.shell.service.change.DirCmd;
import zos.shell.service.console.MvsConsoleCmd;
import zos.shell.service.dsn.DownloadCmd;
import zos.shell.service.dsn.LstCmd;
import zos.shell.service.dsn.concatenate.ConcatCmd;
import zos.shell.service.dsn.copy.CopyCmd;
import zos.shell.service.dsn.delete.DeleteCmd;
import zos.shell.service.env.EnvVarCmd;
import zos.shell.service.grep.GrepCmd;
import zos.shell.service.help.HelpCmd;
import zos.shell.service.job.BrowseCmd;
import zos.shell.service.job.TerminateCmd;
import zos.shell.service.localfile.LocalFileCmd;
import zos.shell.service.omvs.SshCmd;
import zos.shell.service.search.SearchCmd;
import zos.shell.utility.Util;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.methods.JobSubmit;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Commands {

    private static final Logger LOG = LoggerFactory.getLogger(Commands.class);

    private final List<ZosConnection> connections;
    private final TextTerminal<?> terminal;
    private long timeOutValue = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(List<ZosConnection> connections, TextTerminal<?> terminal) {
        LOG.debug("*** Commands ***");
        this.connections = connections;
        this.terminal = terminal;
    }

    public Output browse(ZosConnection connection, String[] params) {
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

    private Output browseAll(ZosConnection connection, String[] params, boolean isAll) {
        LOG.debug("*** browseAll ***");
        final BrowseCmd browseJob = new BrowseCmd(new JobGet(connection), isAll, timeOutValue);
        final ResponseStatus responseStatus = browseJob.browseJob(params[1]);
        if (!responseStatus.isStatus()) {
            terminal.println(responseStatus.getMessage());
            return null;
        }
        final String output = responseStatus.getMessage();
        terminal.println(output);
        return new Output(params[1], new StringBuilder(output));
    }

    public void cancel(ZosConnection connection, String jobOrTask) {
        LOG.debug("*** cancel ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(
                new FutureTerminate(connection, new IssueConsole(connection), TerminateCmd.Type.CANCEL, jobOrTask));
        processFuture(pool, submit);
    }

    public Output cat(ZosConnection connection, String dataset, String target) {
        LOG.debug("*** cat ***");
        final var concatCmd = new ConcatCmd(new DownloadCmd(new DsnGet(connection), false), timeOutValue);
        final var data = concatCmd.cat(dataset, target).getMessage();
        return new Output("cat", new StringBuilder(data));
    }

    public String cd(ZosConnection connection, String currDataSet, String param) {
        LOG.debug("*** cd ***");
        return new DirCmd(terminal, new DsnList(connection)).cd(currDataSet, param);
    }

    public ZosConnection change(ZosConnection connection, String[] commands) {
        LOG.debug("*** change ***");
        final var changeConn = new ConnCmd(terminal, connections);
        return changeConn.changeConnection(connection, commands);
    }

    public void color(String arg, String agr2) {
        LOG.debug("*** color ***");
        final var color = new ColorCmd(terminal);
        color.setTextColor(arg);
        color.setBackGroundColor(agr2);
    }

    public void connections(ZosConnection connection) {
        LOG.debug("*** connections ***");
        final var changeConn = new ConnCmd(terminal, connections);
        changeConn.displayConnections(connection);
    }

    public void copy(ZosConnection connection, String currDataSet, String[] params) {
        LOG.debug("*** copy ***");
        final CopyCmd copy = new CopyCmd(connection, new DsnList(connection), timeOutValue);
        terminal.println(copy.copy(currDataSet, params).getMessage());
    }

    public void count(ZosConnection connection, String dataSet, String filter) {
        LOG.debug("*** count ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureCount(new DsnList(connection), dataSet, filter));
        processFuture(pool, submit);
    }

    public void download(ZosConnection connection, String currDataSet, String target, boolean isBinary) {
        LOG.debug("*** download ***");
        if ("*".equals(target)) {
            final var members = Util.getMembers(terminal, connection, currDataSet);
            if (members.isEmpty()) {
                terminal.println(Constants.DOWNLOAD_NOTHING_WARNING);
                return;
            }
            multipleDownload(connection, currDataSet, members, isBinary).forEach(i -> terminal.println(i.getMessage()));
            return;
        }

        if (target.contains("*") && Util.isMember(target.substring(0, target.indexOf("*")))) {
            var members = Util.getMembers(terminal, connection, currDataSet);
            final var index = target.indexOf("*");
            final var searchForMember = target.substring(0, index).toUpperCase();
            members = members.stream().filter(i -> i.startsWith(searchForMember)).collect(Collectors.toList());
            if (members.isEmpty()) {
                terminal.println(Constants.DOWNLOAD_NOTHING_WARNING);
                return;
            }
            multipleDownload(connection, currDataSet, members, isBinary).forEach(i -> terminal.println(i.getMessage()));
            return;
        }

        final DownloadCmd download = new DownloadCmd(new DsnGet(connection), isBinary);

        final var dataSetMember = Util.getDatasetAndMember(target);
        ResponseStatus result;
        if (dataSetMember != null) {
            result = download.download(dataSetMember.getDataSet(), dataSetMember.getMember());
        } else if (Util.isMember(target)) {
            result = download.download(currDataSet, target);
        } else {
            result = download.download(target);
        }

        if (!result.isStatus()) {
            terminal.println(Util.getMsgAfterArrow(result.getMessage()));
            terminal.println("cannot download " + target + ", try again...");
        } else {
            terminal.println(result.getMessage());
            Util.openFileLocation(result.getOptionalData());
        }
    }

    public void downloadJob(ZosConnection currConnection, String param, boolean isAll) {
        LOG.debug("*** downloadJob ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureDownloadJob(new JobGet(currConnection), isAll, timeOutValue, param));
        processFuture(pool, submit);
    }

    private List<ResponseStatus> multipleDownload(ZosConnection connection, String dataSet, List<String> members,
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
            futures.add(pool.submit(new FutureDownload(new DsnGet(connection), dataSet, member, isBinary)));
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
                results.add(new ResponseStatus(e.getMessage(), false));
            }
        }
        return results;
    }

    public Output env() {
        LOG.debug("*** env ***");
        final var env = EnvVarCmd.getInstance();
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
        LocalFileCmd.listFiles(terminal, dataSet);
    }

    public void grep(ZosConnection connection, String pattern, String target, String currDataSet) {
        LOG.debug("*** grep ***");
        List<String> result;

        if ("*".equals(target)) {
            final var members = Util.getMembers(terminal, connection, currDataSet);
            result = multipleGrep(connection, pattern, currDataSet, members);
            result.forEach(terminal::println);
            if (result.isEmpty()) {
                terminal.println(Constants.NOTHING_FOUND);
            }
            return;
        }

        if (target.contains("*") && Util.isMember(target.substring(0, target.indexOf("*")))) {
            var members = Util.getMembers(terminal, connection, currDataSet);
            final var searchForMember = target.substring(0, target.indexOf("*")).toUpperCase();
            members = members.stream().filter(i -> i.startsWith(searchForMember)).collect(Collectors.toList());
            result = multipleGrep(connection, pattern, currDataSet, members);
            result.forEach(terminal::println);
            if (result.isEmpty()) {
                terminal.println(Constants.NOTHING_FOUND);
            }
            return;
        }

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var concatCmd = new ConcatCmd(new DownloadCmd(new DsnGet(connection), false), timeOutValue);
        final var submit = pool.submit(new FutureGrep(new GrepCmd(concatCmd, pattern), currDataSet, target));

        try {
            result = submit.get(timeOutValue, TimeUnit.SECONDS);
            result.forEach(terminal::println);
            if (result.isEmpty()) {
                terminal.println(Constants.NOTHING_FOUND);
            }
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(e.getMessage());
        }

        pool.shutdownNow();
    }

    private List<String> multipleGrep(ZosConnection connection, String pattern, String dataSet, List<String> members) {
        LOG.debug("*** multipleGrep ***");
        final var results = new ArrayList<String>();
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        final var futures = new ArrayList<Future<List<String>>>();

        for (final var member : members) {
            final var concatCmd = new ConcatCmd(new DownloadCmd(new DsnGet(connection), false), timeOutValue);
            futures.add(pool.submit(new FutureGrep(new GrepCmd(concatCmd, pattern, true), dataSet, member)));
        }

        for (final var future : futures) {
            try {
                results.addAll(future.get(timeOutValue, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                results.add("timeout");
            } catch (InterruptedException | ExecutionException e) {
                results.add(e.getMessage());
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
        return HelpCmd.display(terminal);
    }

    public void ls(ZosConnection connection, String member, String dataSet) {
        LOG.debug("*** ls 1 ***");
        final var listing = new LstCmd(terminal, new DsnList(connection), timeOutValue);
        try {
            listing.ls(member, dataSet, true, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void ls(ZosConnection connection, String dataSet) {
        LOG.debug("*** ls 2 ***");
        final var listing = new LstCmd(terminal, new DsnList(connection), timeOutValue);
        try {
            listing.ls(null, dataSet, true, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void lsl(ZosConnection connection, String dataSet, boolean isAttributes) {
        LOG.debug("*** lsl 1 ***");
        this.lsl(connection, null, dataSet, isAttributes);
    }

    public void lsl(ZosConnection connection, String member, String dataSet, boolean isAttributes) {
        LOG.debug("*** lsl 2 ***");
        final var listing = new LstCmd(terminal, new DsnList(connection), timeOutValue);
        try {
            listing.ls(member, dataSet, false, isAttributes);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void mkdir(ZosConnection connection, TextIO mainTextIO, String currDataSet, String param) {
        LOG.debug("*** mkdir ***");
        if (param.contains(".") && !Util.isDataSet(param)) {
            terminal.println("invalid data set character sequence, try again...");
            return;
        }
        if (!param.contains(".") && !Util.isMember(param)) {
            terminal.println("invalid 8 character sequence, try again...");
            return;
        }

        if (!Util.isDataSet(param) && Util.isMember(param) && !currDataSet.isBlank()) {
            param = currDataSet + "." + param;
        } else if (Util.isMember(param) && currDataSet.isBlank()) {
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
        final var submit = pool.submit(new FutureMakeDirectory(new DsnCreate(connection), param, createParams));
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
            } catch (NumberFormatException ignored) {
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

    public Output mvsCommand(ZosConnection connection, String command) {
        LOG.debug("*** mvsCommand ***");
        final var mvsConsoleCmd = new MvsConsoleCmd(connection, timeOutValue);
        final var responseStatus = mvsConsoleCmd.issueConsoleCmd(command);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
        return new Output("mvs", new StringBuilder(responseStatus.getMessage()));
    }

    public Output ps(ZosConnection connection) {
        LOG.debug("*** ps ***");
        return ps(connection, null);
    }

    public void purgeJob(ZosConnection connection, String item) {
        LOG.debug("*** purgeJob ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FuturePurgeJob(connection, item));
        processFuture(pool, submit);
    }

    public Output ps(ZosConnection connection, String jobOrTask) {
        LOG.debug("*** ps ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureProcessList(new JobGet(connection), jobOrTask));
        final var response = processFuture(pool, submit);
        return response != null && response.isStatus() ? new Output("ps",
                new StringBuilder(response.getMessage())) : null;
    }

    public void rm(ZosConnection connection, String currDataSet, String param) {
        LOG.debug("*** rm ***");
        final var delete = new DeleteCmd(connection, new DsnList(connection), timeOutValue);
        terminal.println(delete.delete(currDataSet, param).getMessage());
    }

    public void save(ZosConnection connection, String dataSet, String[] params) {
        LOG.debug("*** save ***");
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureSave(new DsnWrite(connection), dataSet, member));
        processFuture(pool, submit);
    }

    public void search(Output output, String text) {
        LOG.debug("*** search ***");
        final var search = new SearchCmd(terminal);
        search.search(output, text);
    }

    public void set(final String param) {
        LOG.debug("*** set ***");
        final var values = param.split("=");
        if (values.length != 2) {
            terminal.println(Constants.INVALID_COMMAND);
            return;
        }
        EnvVarCmd.getInstance().setVariable(values[0], values[1]);
        terminal.println(values[0] + "=" + values[1]);
    }

    public void stop(ZosConnection connection, String jobOrTask) {
        LOG.debug("*** stop ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(
                new FutureTerminate(connection, new IssueConsole(connection), TerminateCmd.Type.STOP, jobOrTask));
        processFuture(pool, submit);
    }

    public void submit(ZosConnection connection, String dataSet, String jobName) {
        LOG.debug("*** submit ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureSubmit(new JobSubmit(connection), dataSet, jobName));
        processFuture(pool, submit);
    }

    public Output tailJob(ZosConnection connection, String[] params) {
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

    private Output processTailJobResponse(ZosConnection connection, String[] params, boolean isAll) {
        LOG.debug("*** processTailJobResponse ***");
        final var response = tailAll(connection, params, isAll);
        return response != null && response.isStatus() ? new Output(params[1],
                new StringBuilder(response.getMessage())) : null;
    }

    private ResponseStatus tailAll(ZosConnection connection, String[] params, boolean isAll) {
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

    public void touch(ZosConnection connection, String dataSet, String[] params) {
        LOG.debug("*** touch ***");
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTouch(new DsnWrite(connection),
                new Member(new DsnList(connection)), dataSet, member));
        processFuture(pool, submit);
    }

    public Output tsoCommand(ZosConnection connection, String accountNumber, String command) {
        LOG.debug("*** tsoCommand ***");
        if (accountNumber == null) {
            terminal.println("ACCTNUM is not set, try again...");
            return new Output("tso", new StringBuilder());
        }
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTso(connection, accountNumber, command));
        final var response = processFuture(pool, submit);
        if (response != null && response.isStatus()) {
            return new Output("tso", new StringBuilder(response.getMessage()));
        } else {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
            return null;
        }
    }

    public void uname(ZosConnection currConnection) {
        LOG.debug("*** uname ***");
        if (currConnection != null) {
            Optional<String> zosVersion = Optional.empty();
            try {
                final var IssueConsole = new IssueConsole(currConnection);
                final var response = IssueConsole.issueCommand("D IPLINFO");
                final var output = response.getCommandResponse()
                        .orElseThrow((() -> new ZosmfRequestException("IPLINFO command no response")));
                final var index = output.indexOf("RELEASE z/OS ");
                if (index >= 0) {
                    zosVersion = Optional.of(output.substring(index, index + 22));
                }
            } catch (ZosmfRequestException e) {
                LOG.debug(e.getMessage());
            }
            terminal.println(
                    "hostname: " + currConnection.getHost() + ", " + zosVersion.orElse("n\\a"));
        } else {
            terminal.println(Constants.NO_INFO);
        }
    }

    public void ussh(TextTerminal<?> terminal, ZosConnection currConnection,
                     Map<String, SshConnection> SshConnections, String param) {
        LOG.debug("*** ussh ***");
        final var uss = new SshCmd(terminal, currConnection, SshConnections);
        uss.sshCommand(param);
    }

    public void vi(ZosConnection connection, String dataSet, String[] params) {
        LOG.debug("*** vi ***");
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureVi(
                new DownloadCmd(new DsnGet(connection), false), dataSet, member));
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
            terminal.println(e.getMessage());
        }
        pool.shutdownNow();
        return result;
    }

}
