package zos.shell.controller;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.change.ChangeConnectionService;
import zos.shell.service.change.ChangeDirectoryService;
import zos.shell.service.change.ChangeWindowService;
import zos.shell.service.dsn.copy.CopyService;
import zos.shell.service.dsn.delete.DeleteService;
import zos.shell.service.dsn.download.DownloadDsnService;
import zos.shell.service.dsn.list.ListingService;
import zos.shell.service.dsn.makedir.MakeDirectoryService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.localfile.LocalFileService;
import zos.shell.service.search.SearchCache;
import zos.shell.service.search.SearchCacheService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;

import java.util.TreeMap;

public class Commands {

    private static final Logger LOG = LoggerFactory.getLogger(Commands.class);

    private final TextTerminal<?> terminal;
    private final long timeout = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(final TextTerminal<?> terminal) {
        LOG.debug("*** Commands ***");
        this.terminal = terminal;
    }

//    public SearchCache cat(final ZosConnection connection, final String dataset, final String target) {
//        LOG.debug("*** cat ***");
//        final var concatCmd = new ConcatService(new Download(new DsnGet(connection), false), timeout);
//        final var data = concatCmd.cat(dataset, target).getMessage();
//        terminal.println(data);
//        return new SearchCache("cat", new StringBuilder(data));
//    }

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

    public void rm(final ZosConnection connection, final String dataset, final String param) {
        LOG.debug("*** rm ***");
        final var delete = new DeleteService(connection, timeout);
        final var responseStatus = delete.delete(dataset, param);
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

}
