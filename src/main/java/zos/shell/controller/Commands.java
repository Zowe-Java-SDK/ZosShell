package zos.shell.controller;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.future.FutureDownload;
import zos.shell.future.FutureDownloadJob;
import zos.shell.response.ResponseStatus;
import zos.shell.service.change.ColorCmd;
import zos.shell.service.change.ConnCmd;
import zos.shell.service.change.DirCmd;
import zos.shell.service.console.MvsConsoleCmd;
import zos.shell.service.dsn.DownloadCmd;
import zos.shell.service.dsn.concatenate.ConcatCmd;
import zos.shell.service.dsn.copy.CopyCmd;
import zos.shell.service.dsn.count.CountCmd;
import zos.shell.service.dsn.delete.DeleteCmd;
import zos.shell.service.dsn.edit.EditCmd;
import zos.shell.service.dsn.list.LstCmd;
import zos.shell.service.dsn.makedir.MakeDirCmd;
import zos.shell.service.dsn.save.SaveCmd;
import zos.shell.service.dsn.touch.TouchCmd;
import zos.shell.service.env.EnvVarCmd;
import zos.shell.service.grep.GrepCmd;
import zos.shell.service.help.HelpCmd;
import zos.shell.service.job.BrowseCmd;
import zos.shell.service.job.processlst.ProcessLstCmd;
import zos.shell.service.job.purge.PurgeCmd;
import zos.shell.service.job.submit.SubmitCmd;
import zos.shell.service.job.tail.TailCmd;
import zos.shell.service.job.terminate.TerminateCmd;
import zos.shell.service.localfile.LocalFileCmd;
import zos.shell.service.omvs.SshCmd;
import zos.shell.service.search.SearchCache;
import zos.shell.service.search.SearchCmd;
import zos.shell.service.tso.TsoCmd;
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
import zowe.client.sdk.zosjobs.methods.JobDelete;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.methods.JobSubmit;
import zowe.client.sdk.zostso.method.IssueTso;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Commands {

    private static final Logger LOG = LoggerFactory.getLogger(Commands.class);

    private final List<ZosConnection> connections;
    private final TextTerminal<?> terminal;
    private long timeout = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(final List<ZosConnection> connections, final TextTerminal<?> terminal) {
        LOG.debug("*** Commands ***");
        this.connections = connections;
        this.terminal = terminal;
    }

    public SearchCache browse(final ZosConnection connection, final String[] params) {
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

    private SearchCache browseAll(final ZosConnection connection, final String[] params, boolean isAll) {
        LOG.debug("*** browseAll ***");
        final var browseJob = new BrowseCmd(new JobGet(connection), isAll, timeout);
        final var responseStatus = browseJob.browseJob(params[1]);
        if (!responseStatus.isStatus()) {
            terminal.println(responseStatus.getMessage());
            return null;
        }
        final String output = responseStatus.getMessage();
        terminal.println(output);
        return new SearchCache(params[1], new StringBuilder(output));
    }

    public void cancel(final ZosConnection connection, final String target) {
        LOG.debug("*** cancel ***");
        final var terminateCmd = new TerminateCmd(connection, new IssueConsole(connection), timeout);
        final var responseStatus = terminateCmd.terminate(TerminateCmd.Type.CANCEL, target);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    public SearchCache cat(final ZosConnection connection, final String dataset, final String target) {
        LOG.debug("*** cat ***");
        final var concatCmd = new ConcatCmd(new DownloadCmd(new DsnGet(connection), false), timeout);
        final var data = concatCmd.cat(dataset, target).getMessage();
        terminal.println(data);
        return new SearchCache("cat", new StringBuilder(data));
    }

    public String cd(final ZosConnection connection, final String dataset, final String param) {
        LOG.debug("*** cd ***");
        return new DirCmd(terminal, new DsnList(connection)).cd(dataset, param);
    }

    public ZosConnection change(final ZosConnection connection, final String[] commands) {
        LOG.debug("*** change ***");
        final var changeConn = new ConnCmd(terminal, connections);
        return changeConn.changeConnection(connection, commands);
    }

    public void color(final String arg, final String agr2) {
        LOG.debug("*** color ***");
        final var color = new ColorCmd(terminal);
        color.setTextColor(arg);
        color.setBackGroundColor(agr2);
    }

    public void connections(final ZosConnection connection) {
        LOG.debug("*** connections ***");
        final var changeConn = new ConnCmd(terminal, connections);
        changeConn.displayConnections(connection);
    }

    public void copy(final ZosConnection connection, final String dataset, final String[] params) {
        LOG.debug("*** copy ***");
        final var copy = new CopyCmd(connection, new DsnList(connection), timeout);
        terminal.println(copy.copy(dataset, params).getMessage());
    }

    public void count(final ZosConnection connection, final String dataset, final String filter) {
        LOG.debug("*** count ***");
        final var countcmd = new CountCmd(new DsnList(connection), timeout);
        final var responseStatus = countcmd.count(dataset, filter);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    public void download(final ZosConnection connection, final String dataset,
                         final String target, boolean isBinary) {
        LOG.debug("*** download ***");
        if ("*".equals(target)) {
            final var members = Util.getMembers(terminal, connection, dataset);
            if (members.isEmpty()) {
                terminal.println(Constants.DOWNLOAD_NOTHING_WARNING);
                return;
            }
            multipleDownload(connection, dataset, members, isBinary).forEach(i -> terminal.println(i.getMessage()));
            return;
        }

        if (target.contains("*") && Util.isMember(target.substring(0, target.indexOf("*")))) {
            var members = Util.getMembers(terminal, connection, dataset);
            final var index = target.indexOf("*");
            final var searchForMember = target.substring(0, index).toUpperCase();
            members = members.stream().filter(i -> i.startsWith(searchForMember)).collect(Collectors.toList());
            if (members.isEmpty()) {
                terminal.println(Constants.DOWNLOAD_NOTHING_WARNING);
                return;
            }
            multipleDownload(connection, dataset, members, isBinary).forEach(i -> terminal.println(i.getMessage()));
            return;
        }

        final DownloadCmd download = new DownloadCmd(new DsnGet(connection), isBinary);

        final var dataSetMember = Util.getDatasetAndMember(target);
        ResponseStatus result;
        if (dataSetMember != null) {
            result = download.download(dataSetMember.getDataSet(), dataSetMember.getMember());
        } else if (Util.isMember(target)) {
            result = download.download(dataset, target);
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

    public void downloadJob(final ZosConnection connection, final String param, boolean isAll) {
        LOG.debug("*** downloadJob ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureDownloadJob(new JobGet(connection), isAll, timeout, param));
        processFuture(pool, submit);
    }

    private List<ResponseStatus> multipleDownload(final ZosConnection connection, final String dataset,
                                                  final List<String> members, boolean isBinary) {
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
            futures.add(pool.submit(new FutureDownload(new DsnGet(connection), dataset, member, isBinary)));
        }

        final var result = getFutureResults(futures);
        Util.openFileLocation(result.get(0).getOptionalData());
        pool.shutdownNow();
        return result;
    }

    private List<ResponseStatus> getFutureResults(final List<Future<ResponseStatus>> futures) {
        LOG.debug("*** getFutureResults ***");
        final var results = new ArrayList<ResponseStatus>();
        for (final var future : futures) {
            try {
                results.add(future.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e ) {
                future.cancel(true);
                results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
            }
        }
        return results;
    }

    public SearchCache env() {
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
        return new SearchCache("env", str);
    }

    public void files(String dataSet) {
        LOG.debug("*** files ***");
        LocalFileCmd.listFiles(terminal, dataSet);
    }

    public void grep(final ZosConnection connection, final String pattern, final String target, final String dataset) {
        LOG.debug("*** grep ***");
        final var grepCmd = new GrepCmd(connection, pattern, timeout);
        grepCmd.search(dataset, target).forEach(i -> {
            if (i.endsWith("\n")) {
                terminal.print(i);
            } else {
                terminal.println(i);
            }
        });
    }

    public SearchCache help() {
        LOG.debug("*** help ***");
        return HelpCmd.display(terminal);
    }

    public void ls(final ZosConnection connection, final String member, final String dataset) {
        LOG.debug("*** ls 1 ***");
        final var listing = new LstCmd(terminal, new DsnList(connection), timeout);
        try {
            listing.ls(member, dataset, true, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void ls(final ZosConnection connection, final String dataset) {
        LOG.debug("*** ls 2 ***");
        final var listing = new LstCmd(terminal, new DsnList(connection), timeout);
        try {
            listing.ls(null, dataset, true, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void lsl(final ZosConnection connection, final String dataset, boolean isAttributes) {
        LOG.debug("*** lsl 1 ***");
        this.lsl(connection, null, dataset, isAttributes);
    }

    public void lsl(final ZosConnection connection, final String member, final String dataset, boolean isAttributes) {
        LOG.debug("*** lsl 2 ***");
        final var listing = new LstCmd(terminal, new DsnList(connection), timeout);
        try {
            listing.ls(member, dataset, false, isAttributes);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        }
    }

    public void mkdir(final ZosConnection connection, final TextIO mainTextIO,
                      final String dataset, String param) {
        LOG.debug("*** mkdir ***");
        if (param.contains(".") && !Util.isDataSet(param)) {
            terminal.println("invalid data set character sequence, try again...");
            return;
        }
        if (!param.contains(".") && !Util.isMember(param)) {
            terminal.println("invalid 8 character sequence, try again...");
            return;
        }

        if (!Util.isDataSet(param) && Util.isMember(param) && !dataset.isBlank()) {
            param = dataset + "." + param;
        } else if (Util.isMember(param) && dataset.isBlank()) {
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

        final var makeDirCmd = new MakeDirCmd(new DsnCreate(connection), timeout);
        final var responseStatus = makeDirCmd.create(param, createParams);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    private static Integer getMakeDirNum(final TextIO mainTextIO, final String prompt) {
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

    private static String getMakeDirStr(final TextIO mainTextIO, final String prompt) {
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

    public SearchCache mvsCommand(final ZosConnection connection, final String command) {
        LOG.debug("*** mvsCommand ***");
        final var mvsConsoleCmd = new MvsConsoleCmd(connection, timeout);
        final var responseStatus = mvsConsoleCmd.issueConsoleCmd(command);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
        return new SearchCache("mvs", new StringBuilder(responseStatus.getMessage()));
    }

    public SearchCache ps(final ZosConnection connection) {
        LOG.debug("*** ps ***");
        return ps(connection, null);
    }

    public void purge(final ZosConnection connection, final String filter) {
        LOG.debug("*** purgeJob ***");
        final var purgeCmd = new PurgeCmd(new JobDelete(connection), new JobGet(connection), timeout);
        final var responseStatus = purgeCmd.purge(filter);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    public SearchCache ps(final ZosConnection connection, final String target) {
        LOG.debug("*** ps ***");
        final var processLstCmd = new ProcessLstCmd(new JobGet(connection), timeout);
        final var responseStatus = processLstCmd.processLst(target);
        terminal.println(responseStatus.getMessage());
        return new SearchCache("ps", new StringBuilder(responseStatus.getMessage()));
    }

    public void rm(final ZosConnection connection, final String dataset, final String param) {
        LOG.debug("*** rm ***");
        final var delete = new DeleteCmd(connection, new DsnList(connection), timeout);
        terminal.println(delete.delete(dataset, param).getMessage());
    }

    public void save(final ZosConnection connection, final String dataset, final String[] params) {
        LOG.debug("*** save ***");
        final var saveCmd = new SaveCmd(new DsnWrite(connection), timeout);
        final var responseStatus = saveCmd.save(dataset, params[1]);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    public void search(final SearchCache output, final String text) {
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

    public void stop(final ZosConnection connection, final String target) {
        LOG.debug("*** stop ***");
        final var terminateCmd = new TerminateCmd(connection, new IssueConsole(connection), timeout);
        final var responseStatus = terminateCmd.terminate(TerminateCmd.Type.STOP, target);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    public void submit(final ZosConnection connection, final String dataset, final String target) {
        LOG.debug("*** submit ***");
        final var submitCmd = new SubmitCmd(new JobSubmit(connection), timeout);
        final var responseStatus = submitCmd.submit(dataset, target);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    public SearchCache tail(final ZosConnection connection, final String[] params) {
        LOG.debug("*** tail ***");
        final var tailCmd = new TailCmd(terminal, new JobGet(connection), timeout);
        long allCount = Arrays.stream(params).filter("ALL"::equalsIgnoreCase).count();
        final var responseStatus = tailCmd.tail(params, allCount == 1);
        if (!responseStatus.isStatus()) {
            terminal.println(responseStatus.getMessage());
            return new SearchCache("tail", new StringBuilder());
        }
        return new SearchCache("tail", new StringBuilder(responseStatus.getMessage()));
    }

    public void timeout(final long value) {
        LOG.debug("*** timeout ***");
        timeout = value;
        terminal.println("timeout value set to " + timeout + " seconds.");
    }

    public void timeout() {
        LOG.debug("*** timeout ***");
        terminal.println("timeout value is " + timeout + " seconds.");
    }

    public void touch(final ZosConnection connection, final String dataset, final String[] params) {
        LOG.debug("*** touch ***");
        final var touchCmd = new TouchCmd(new DsnWrite(connection), new DsnList(connection), timeout);
        final var responseStatus = touchCmd.touch(dataset, params[1]);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    public SearchCache tsoCommand(final ZosConnection connection, final String accountNum, final String command) {
        LOG.debug("*** tsoCommand ***");
        if (accountNum == null || accountNum.isBlank()) {
            terminal.println("ACCTNUM is not set, try again...");
            // TODO send null instead to cache how would search command react should not null error out...
            return new SearchCache("tso", new StringBuilder());
        }
        final var tsoCmd = new TsoCmd(new IssueTso(connection), accountNum, timeout);
        final var responseStatus = tsoCmd.issueCommand(command);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
        return new SearchCache("tso", new StringBuilder(responseStatus.getMessage()));
    }

    public void uname(final ZosConnection connection) {
        LOG.debug("*** uname ***");
        if (connection != null) {
            Optional<String> zosVersion = Optional.empty();
            try {
                final var IssueConsole = new IssueConsole(connection);
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
                    "hostname: " + connection.getHost() + ", " + zosVersion.orElse("n\\a"));
        } else {
            terminal.println(Constants.NO_INFO);
        }
    }

    public void ussh(final TextTerminal<?> terminal, final ZosConnection zosConnection,
                     final Map<String, SshConnection> SshConnections, final String param) {
        LOG.debug("*** ussh ***");
        final var uss = new SshCmd(terminal, zosConnection, SshConnections);
        uss.sshCommand(param);
    }

    public void vi(final ZosConnection connection, final String dataset, final String[] params) {
        LOG.debug("*** vi ***");
        final var editCmd = new EditCmd(new DownloadCmd(new DsnGet(connection), false), timeout);
        final var responseStatus = editCmd.open(dataset, params[1]);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    private ResponseStatus processFuture(final ExecutorService pool, final Future<ResponseStatus> submit) {
        LOG.debug("*** processFuture ***");
        ResponseStatus result = null;
        try {
            result = submit.get(timeout, TimeUnit.SECONDS);
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
