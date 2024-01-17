package zos.shell.controller;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.dsn.makedir.MakeDirectoryService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.localfile.LocalFileService;
import zos.shell.service.search.SearchCache;
import zos.shell.service.search.SearchCacheService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.TreeMap;

public class Commands {

    private static final Logger LOG = LoggerFactory.getLogger(Commands.class);

    private final TextTerminal<?> terminal;
    private final long timeout = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(final TextTerminal<?> terminal) {
        LOG.debug("*** Commands ***");
        this.terminal = terminal;
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
