package zos.shell.controller;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.change.ChangeConnectionService;
import zos.shell.service.change.ChangeDirectoryService;
import zos.shell.service.change.ChangeWindowService;
import zos.shell.service.console.ConsoleService;
import zos.shell.service.dsn.concat.ConcatService;
import zos.shell.service.dsn.copy.CopyService;
import zos.shell.service.dsn.count.CountService;
import zos.shell.service.dsn.delete.DeleteService;
import zos.shell.service.dsn.download.Download;
import zos.shell.service.dsn.download.DownloadDsnService;
import zos.shell.service.dsn.edit.EditService;
import zos.shell.service.dsn.list.ListingService;
import zos.shell.service.dsn.makedir.MakeDirectoryService;
import zos.shell.service.dsn.save.SaveService;
import zos.shell.service.dsn.touch.TouchService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.grep.GrepService;
import zos.shell.service.help.HelpService;
import zos.shell.service.job.browse.BrowseLogService;
import zos.shell.service.job.download.DownloadJobService;
import zos.shell.service.job.processlst.ProcessListingService;
import zos.shell.service.job.purge.PurgeService;
import zos.shell.service.job.submit.SubmitService;
import zos.shell.service.job.tail.TailService;
import zos.shell.service.job.terminate.TerminateService;
import zos.shell.service.localfile.LocalFileService;
import zos.shell.service.omvs.SshService;
import zos.shell.service.search.SearchCache;
import zos.shell.service.search.SearchCacheService;
import zos.shell.service.tso.TsoService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
import zos.shell.utility.StrUtil;
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

import java.util.Arrays;
import java.util.Optional;
import java.util.TreeMap;

public class Commands {

    private static final Logger LOG = LoggerFactory.getLogger(Commands.class);

    private final TextTerminal<?> terminal;
    private long timeout = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(final TextTerminal<?> terminal) {
        LOG.debug("*** Commands ***");
        this.terminal = terminal;
    }

    public SearchCache browse(final ZosConnection connection, final String target, boolean isAll) {
        LOG.debug("*** browse ***");
        final var browseCmd = new BrowseLogService(new JobGet(connection), isAll, timeout);
        final var responseStatus = browseCmd.browseJob(target);
        final String output = responseStatus.getMessage();
        terminal.println(output);
        return new SearchCache(target, new StringBuilder(output));
    }

    public void cancel(final ZosConnection connection, final String target) {
        LOG.debug("*** cancel ***");
        final var terminateCmd = new TerminateService(new IssueConsole(connection), timeout);
        final var responseStatus = terminateCmd.terminate(TerminateService.Type.CANCEL, target);
        terminal.println(responseStatus.getMessage());
    }

    public SearchCache cat(final ZosConnection connection, final String dataset, final String target) {
        LOG.debug("*** cat ***");
        final var concatCmd = new ConcatService(new Download(new DsnGet(connection), false), timeout);
        final var data = concatCmd.cat(dataset, target).getMessage();
        terminal.println(data);
        return new SearchCache("cat", new StringBuilder(data));
    }

    public String cd(final ZosConnection connection, final String dataset, final String param) {
        LOG.debug("*** cd ***");
        return new ChangeDirectoryService(terminal, new DsnList(connection)).cd(dataset, param);
    }

    public ZosConnection changeZosConnection(final ZosConnection connection, final String[] commands) {
        LOG.debug("*** changeZosConnection ***");
        final var changeConn = new ChangeConnectionService(terminal);
        return changeConn.changeZosConnection(connection, commands);
    }

    public SshConnection changeSshConnection(final SshConnection connection, final String[] commands) {
        LOG.debug("*** changeSshConnection ***");
        final var changeConn = new ChangeConnectionService(terminal);
        return changeConn.changeSshConnection(connection, commands);
    }

    public void color(final String arg, final String agr2) {
        LOG.debug("*** color ***");
        final var color = new ChangeWindowService(terminal);
        final var str = new StringBuilder();
        String result;
        result = color.setTextColor(arg);
        str.append(result != null ? result + "\n" : "");
        result = color.setBackGroundColor(agr2);
        str.append(result != null ? result : "");
        terminal.println(str.toString());
    }

    public void copy(final ZosConnection connection, final String dataset, final String[] params) {
        LOG.debug("*** copy ***");
        final var copy = new CopyService(connection, timeout);
        terminal.println(copy.copy(dataset, params).getMessage());
    }

    public void count(final ZosConnection connection, final String dataset, final String filter) {
        LOG.debug("*** count ***");
        final var countcmd = new CountService(new DsnList(connection), timeout);
        final var responseStatus = countcmd.count(dataset, filter);
        terminal.println(responseStatus.getMessage());
    }

    public void displayConnections() {
        LOG.debug("*** displayConnections ***");
        new ChangeConnectionService(terminal).displayConnections();
    }

    public void downloadDsn(final ZosConnection connection, final String dataset,
                            final String target, boolean isBinary) {
        LOG.debug("*** dsnDownload ***");
        final var downloadDsnCmd = new DownloadDsnService(connection, isBinary, timeout);
        final var results = downloadDsnCmd.download(dataset, target);
        if (results.size() > 1) {
            results.forEach(r -> terminal.println(r.getMessage()));
        } else {
            terminal.println(ResponseUtil.getMsgAfterArrow(results.get(0).getMessage()));
        }
        if (results.size() == 1 && !results.get(0).isStatus()) {
            terminal.println("cannot download " + target + ", try again...");
        }
    }

    public void downloadJob(final ZosConnection connection, final String target, boolean isAll) {
        LOG.debug("*** downloadJob ***");
        final var downloadJobCmd = new DownloadJobService(new JobGet(connection), isAll, timeout);
        final var responseStatus = downloadJobCmd.download(target);
        terminal.println(responseStatus.getMessage());
    }

    public SearchCache env() {
        LOG.debug("*** env ***");
        final var env = EnvVariableService.getInstance();
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

    public SearchCache files(String dataset) {
        LOG.debug("*** files ***");
        LocalFileService localFileService = new LocalFileService();
        StringBuilder result = localFileService.listFiles(dataset);
        terminal.println(result.toString());
        return new SearchCache("files", result);
    }

    public void grep(final ZosConnection connection, final String pattern, final String target, final String dataset) {
        LOG.debug("*** grep ***");
        final var grepCmd = new GrepService(connection, pattern, timeout);
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
        return HelpService.display(terminal);
    }

    public void ls(final ZosConnection connection, final String member, final String dataset) {
        LOG.debug("*** ls 1 ***");
        final var listing = new ListingService(terminal, new DsnList(connection), timeout);
        try {
            listing.ls(member, dataset, true, false);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            terminal.println(errMsg != null ? errMsg : e.getMessage());
        }
    }

    public void ls(final ZosConnection connection, final String dataset) {
        LOG.debug("*** ls 2 ***");
        final var listing = new ListingService(terminal, new DsnList(connection), timeout);
        try {
            listing.ls(null, dataset, true, false);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            terminal.println(errMsg != null ? errMsg : e.getMessage());
        }
    }

    public void lsl(final ZosConnection connection, final String dataset, boolean isAttributes) {
        LOG.debug("*** lsl 1 ***");
        this.lsl(connection, null, dataset, isAttributes);
    }

    public void lsl(final ZosConnection connection, final String member, final String dataset, boolean isAttributes) {
        LOG.debug("*** lsl 2 ***");
        final var listing = new ListingService(terminal, new DsnList(connection), timeout);
        try {
            listing.ls(member, dataset, false, isAttributes);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            terminal.println(errMsg != null ? errMsg : e.getMessage());
        }
    }

    public void mkdir(final ZosConnection connection, final TextIO mainTextIO,
                      final String dataset, String param) {
        LOG.debug("*** mkdir ***");
        if (param.contains(".") && !DsnUtil.isDataSet(param)) {
            terminal.println("invalid data set character sequence, try again...");
            return;
        }
        if (!param.contains(".") && !DsnUtil.isMember(param)) {
            terminal.println("invalid 8 character sequence, try again...");
            return;
        }
        if (!DsnUtil.isDataSet(param) && DsnUtil.isMember(param) && !dataset.isBlank()) {
            param = dataset + "." + param;
        } else if (DsnUtil.isMember(param) && dataset.isBlank()) {
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
        final var makeDirCmd = new MakeDirectoryService(new DsnCreate(connection), timeout);
        final var responseStatus = makeDirCmd.create(param, createParams);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(responseStatus.getMessage());
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
        } while (StrUtil.isStrNum(input));
        return input;
    }

    public SearchCache mvsCommand(final ZosConnection connection, final String command) {
        LOG.debug("*** mvsCommand ***");
        final var mvsConsoleCmd = new ConsoleService(connection, timeout);
        final var responseStatus = mvsConsoleCmd.issueConsole(command);
        terminal.println(responseStatus.getMessage());
        return new SearchCache("mvs", new StringBuilder(responseStatus.getMessage()));
    }

    public SearchCache ps(final ZosConnection connection) {
        LOG.debug("*** ps all ***");
        return ps(connection, null);
    }

    public void purge(final ZosConnection connection, final String filter) {
        LOG.debug("*** purgeJob ***");
        final var purgeCmd = new PurgeService(new JobDelete(connection), new JobGet(connection), timeout);
        final var responseStatus = purgeCmd.purge(filter);
        terminal.println(responseStatus.getMessage());
    }

    public SearchCache ps(final ZosConnection connection, final String target) {
        LOG.debug("*** ps target ***");
        final var processLstCmd = new ProcessListingService(new JobGet(connection), timeout);
        final var responseStatus = processLstCmd.processLst(target);
        terminal.println(responseStatus.getMessage());
        return new SearchCache("ps", new StringBuilder(responseStatus.getMessage()));
    }

    public void rm(final ZosConnection connection, final String dataset, final String param) {
        LOG.debug("*** rm ***");
        final var delete = new DeleteService(connection, timeout);
        final var responseStatus = delete.delete(dataset, param);
        terminal.println(responseStatus.getMessage());
    }

    public void save(final ZosConnection connection, final String dataset, final String[] params) {
        LOG.debug("*** save ***");
        final var saveCmd = new SaveService(new DsnWrite(connection), timeout);
        final var responseStatus = saveCmd.save(dataset, params[1]);
        terminal.println(responseStatus.getMessage());
    }

    public void search(final SearchCache output, final String text) {
        LOG.debug("*** search ***");
        final var search = new SearchCacheService(terminal);
        search.search(output, text);
    }

    public void set(final String param) {
        LOG.debug("*** set ***");
        final var values = param.split("=");
        if (values.length != 2) {
            terminal.println(Constants.INVALID_COMMAND);
            return;
        }
        EnvVariableService.getInstance().setVariable(values[0], values[1]);
        terminal.println(values[0] + "=" + values[1]);
    }

    public void stop(final ZosConnection connection, final String target) {
        LOG.debug("*** stop ***");
        final var terminateCmd = new TerminateService(new IssueConsole(connection), timeout);
        final var responseStatus = terminateCmd.terminate(TerminateService.Type.STOP, target);
        terminal.println(responseStatus.getMessage());
    }

    public void submit(final ZosConnection connection, final String dataset, final String target) {
        LOG.debug("*** submit ***");
        final var submitCmd = new SubmitService(new JobSubmit(connection), timeout);
        final var responseStatus = submitCmd.submit(dataset, target);
        terminal.println(responseStatus.getMessage());
    }

    public SearchCache tail(final ZosConnection connection, final String[] params) {
        LOG.debug("*** tail ***");
        final var tailCmd = new TailService(terminal, new JobGet(connection), timeout);
        long allCount = Arrays.stream(params).filter("ALL"::equalsIgnoreCase).count();
        final var responseStatus = tailCmd.tail(params, allCount == 1);
        return new SearchCache("tail", new StringBuilder(responseStatus.getMessage()));
    }

    public void timeout(final long value) {
        LOG.debug("*** timeout set value ***");
        timeout = value;
        terminal.println("timeout value set to " + timeout + " seconds.");
    }

    public void timeout() {
        LOG.debug("*** timeout display ***");
        terminal.println("timeout value is " + timeout + " seconds.");
    }

    public void touch(final ZosConnection connection, final String dataset, final String[] params) {
        LOG.debug("*** touch ***");
        final var touchCmd = new TouchService(new DsnWrite(connection), new DsnList(connection), timeout);
        final var responseStatus = touchCmd.touch(dataset, params[1]);
        terminal.println(responseStatus.getMessage());
    }

    public SearchCache tsoCommand(final ZosConnection connection, final String accountNum, final String command) {
        LOG.debug("*** tsoCommand ***");
        if (accountNum == null || accountNum.isBlank()) {
            terminal.println("ACCTNUM is not set, try again...");
            return new SearchCache("tso", new StringBuilder());
        }
        final var tsoCmd = new TsoService(new IssueTso(connection), accountNum, timeout);
        final var responseStatus = tsoCmd.issueCommand(command);
        terminal.println(responseStatus.getMessage());
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
            terminal.println("hostname: " + connection.getHost() + ", " + zosVersion.orElse("n\\a"));
        } else {
            terminal.println(Constants.NO_INFO);
        }
    }

    public void ussh(final TextTerminal<?> terminal, final SshConnection sshConnection, final String param) {
        LOG.debug("*** ussh ***");
        final var uss = new SshService(terminal, sshConnection);
        uss.sshCommand(param);
    }

    public void vi(final ZosConnection connection, final String dataset, final String[] params) {
        LOG.debug("*** vi ***");
        final var editCmd = new EditService(new Download(new DsnGet(connection), false), timeout);
        final var responseStatus = editCmd.open(dataset, params[1]);
        terminal.println(responseStatus.getMessage());
    }

}
