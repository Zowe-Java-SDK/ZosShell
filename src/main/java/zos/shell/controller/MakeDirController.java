package zos.shell.controller;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.makedir.MakeDirService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;

public class MakeDirController {

    private static final Logger LOG = LoggerFactory.getLogger(MakeDirController.class);

    private final MakeDirService makeDirService;
    private final TextTerminal<?> terminal;

    public MakeDirController(final TextTerminal<?> terminal, final MakeDirService makeDirService) {
        LOG.debug("*** MakeDirController ***");
        this.terminal = terminal;
        this.makeDirService = makeDirService;
    }

    public void mkdir(final TextIO mainTextIO, final String dataset, String target) {
        LOG.debug("*** mkdir ***");
        if (target.contains(".") && !DsnUtil.isDataSet(target)) {
            terminal.println("invalid data set character sequence, try again...");
            return;
        }
        if (!target.contains(".") && !DsnUtil.isMember(target)) {
            terminal.println("invalid 8 character sequence, try again...");
            return;
        }
        if (!DsnUtil.isDataSet(target) && DsnUtil.isMember(target) && !dataset.isBlank()) {
            target = dataset + "." + target;
        } else if (DsnUtil.isMember(target) && dataset.isBlank()) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return;
        }
        var createParamsBuilder = new CreateParams.Builder();
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
        var createParams = createParamsBuilder.build();
        target = target.toUpperCase();
        while (true) {
            input = getMakeDirStr(mainTextIO, "Create " + target + " (y/n)?:");
            if ("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input)) {
                break;
            }
            if ("n".equalsIgnoreCase(input) || "no".equalsIgnoreCase(input)) {
                terminal.println(Constants.MAKE_DIR_EXIT_MSG);
                return;
            }
        }
        ResponseStatus responseStatus = makeDirService.create(target, createParams);
        terminal.println(responseStatus.getMessage());
        if (!responseStatus.isStatus()) {
            terminal.println(responseStatus.getMessage());
            terminal.println(Constants.COMMAND_EXECUTION_ERROR_MSG);
        }
    }

    private static Integer getMakeDirNum(final TextIO mainTextIO, final String prompt) {
        LOG.debug("*** getMakeDirNum ***");
        while (true) {
            String input = mainTextIO.newStringInputReader().withMaxLength(80).read(prompt);
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

}
