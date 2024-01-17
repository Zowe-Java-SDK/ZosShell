package zos.shell;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.configuration.ConfigSingleton;
import zos.shell.constants.Constants;
import zos.shell.controller.*;
import zos.shell.record.DataSetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.autocomplete.SearchCommandService;
import zos.shell.service.change.ChangeConnService;
import zos.shell.service.change.ChangeDirService;
import zos.shell.service.console.ConsoleService;
import zos.shell.service.dsn.concat.ConcatService;
import zos.shell.service.dsn.copy.CopyService;
import zos.shell.service.dsn.count.CountService;
import zos.shell.service.dsn.delete.DeleteService;
import zos.shell.service.dsn.download.Download;
import zos.shell.service.dsn.download.DownloadDsnService;
import zos.shell.service.dsn.edit.EditService;
import zos.shell.service.dsn.list.ListingService;
import zos.shell.service.dsn.save.SaveService;
import zos.shell.service.dsn.touch.TouchService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.grep.GrepService;
import zos.shell.service.help.HelpService;
import zos.shell.service.history.HistoryService;
import zos.shell.service.job.browse.BrowseLogService;
import zos.shell.service.job.download.DownloadJobService;
import zos.shell.service.job.processlst.ProcessLstService;
import zos.shell.service.job.purge.PurgeService;
import zos.shell.service.job.submit.SubmitService;
import zos.shell.service.job.tail.TailService;
import zos.shell.service.job.terminate.TerminateService;
import zos.shell.service.omvs.SshService;
import zos.shell.service.search.SearchCache;
import zos.shell.service.tso.TsoService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.PromptUtil;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;
import zowe.client.sdk.zosjobs.methods.JobDelete;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.methods.JobSubmit;
import zowe.client.sdk.zostso.method.IssueTso;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final Logger LOG = LoggerFactory.getLogger(ZosShell.class);

    private static final ListMultimap<String, String> dataSets = ArrayListMultimap.create();
    private static String currDataSet = "";
    private static int currDataSetMax = 0;
    private static SshConnection currSshConnection;
    private static ZosConnection currConnection;
    private static TextTerminal<?> terminal;
    private static Commands commands;
    private static HistoryService history;
    private static SearchCache commandOutput;
    private static final SwingTextTerminal mainTerminal = new SwingTextTerminal();
    private static final SearchCommandService searchCommandService = new SearchCommandService();
    private static final int defaultFontSize = 10;
    private static int fontSize = defaultFontSize;
    private static boolean fontSizeChanged = false;
    private static boolean disableKeys = false;
    private static TextIO mainTextIO;
    private static long timeout = Constants.FUTURE_TIMEOUT_VALUE;

    public static void main(String[] args) {
        LOG.debug("*** main ***");
        mainTerminal.init();
        setTerminalProperties();
        mainTextIO = new TextIO(mainTerminal);
        try {
            var configSingleton = ConfigSingleton.getInstance();
            configSingleton.readConfig();
            currConnection = configSingleton.getZosConnectionByIndex(0);
            currSshConnection = configSingleton.getSshConnectionByIndex(0);
            if (configSingleton.getConfigSettings().getWindow() != null &&
                    configSingleton.getConfigSettings().getWindow().getFontsize() != null) {
                fontSize = Integer.parseInt(configSingleton.getConfigSettings().getWindow().getFontsize());
            }
        } catch (NumberFormatException | IOException e) {
            mainTerminal.println("Error reading or parsing console.json file, try again...");
            mainTerminal.println("Error: " + e.getMessage());
            mainTerminal.read(true);
            mainTerminal.dispose();
            throw new RuntimeException(e);
        }
        new ZosShell().accept(mainTextIO, null);
    }

    private static void setTerminalProperties() {
        LOG.debug("*** setTerminalProperties ***");
        var title = "";
        if (currConnection != null) {
            title = " - " + currConnection.getHost().toUpperCase();
        }
        mainTerminal.setPaneTitle(Constants.APP_TITLE + title);

        URL iconURL = ZosShell.class.getResource("/image/zowe-icon.png");
        if (iconURL != null) {
            var icon = new ImageIcon(iconURL);
            mainTerminal.getFrame().setIconImage(icon.getImage());
        }

        mainTerminal.registerHandler("ctrl C", t -> {
            t.getTextPane().copy();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("UP", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            history.listUpCommands(PromptUtil.getPrompt());
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("DOWN", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            history.listDownCommands(PromptUtil.getPrompt());
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("shift UP", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            fontSize++;
            mainTerminal.setInputFontSize(fontSize);
            mainTerminal.setPromptFontSize(fontSize);
            mainTerminal.moveToLineStart();
            mainTerminal.print(PromptUtil.getPrompt() + " Increased font size to " + fontSize + ".");
            fontSizeChanged = true;
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("shift DOWN", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            if (fontSize != defaultFontSize) {
                fontSize--;
                mainTerminal.setInputFontSize(fontSize);
                mainTerminal.setPromptFontSize(fontSize);
                mainTerminal.moveToLineStart();
                mainTerminal.print(PromptUtil.getPrompt() + " Decreased font size to " + fontSize + ".");
                fontSizeChanged = true;
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("TAB", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            String[] items = mainTerminal.getTextPane().getText().split(PromptUtil.getPrompt());
            var candidateStr = items[items.length - 1].trim();
            candidateStr = candidateStr.replaceAll("[\\p{Cf}]", "");
            if (candidateStr.contains(" ")) {  // invalid look up
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            List<String> candidateLst = searchCommandService.search(candidateStr);
            if (!candidateLst.isEmpty()) {
                mainTerminal.moveToLineStart();
                if (candidateLst.size() == 1) {
                    mainTerminal.print(PromptUtil.getPrompt() + " " + candidateLst.get(0));
                } else {
                    mainTextIO.newStringInputReader().withDefaultValue("hit enter to skip")
                            .read((PromptUtil.getPrompt() + " " + candidateLst));
                }
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        LOG.debug("*** accept ***");
        terminal = textIO.getTextTerminal();
        terminal.setBookmark("top");
        ConfigSingleton.getInstance().updateWindowSittings(terminal);
        commands = new Commands(terminal);
        history = new HistoryService(terminal);
        if (currConnection == null) {
            terminal.println(Constants.NO_CONNECTIONS);
        } else {
            terminal.println("Connected to " + currConnection.getHost() + " with user " + currConnection.getUser() + ".");
        }

        String[] command;
        var commandLine = "";
        while (!"end".equalsIgnoreCase(commandLine)) {
            commandLine = textIO.newStringInputReader().withMaxLength(80).read(PromptUtil.getPrompt());
            if (fontSizeChanged) {
                terminal.println("Front size set.");
                fontSizeChanged = false;
                continue;
            }
            command = commandLine.split(" ");
            if (Arrays.stream(command).anyMatch(String::isEmpty)) {  // handle multiple empty spaces specified
                command = StrUtil.stripEmptyStrings(command);
            }

            // handle edge case where end user enters prompt as the only input, skip it and continue
            if (command.length == 1 && Constants.DEFAULT_PROMPT.equals(command[0])) {
                continue;
            }
            command = removePrompt(command);
            command = exclamationMark(command);
            if (command == null) {
                continue;
            }

            if ("rm".equals(command[0])) {
                if (isParamsMissing(1, command)) {
                    continue;
                }
                if (isParamsExceeded(2, command)) {
                    continue;
                }

                boolean doIt = false;
                do {
                    final var dataSetMember = DataSetMember.getDatasetAndMember(command[1]);
                    if (!currDataSet.isBlank() && dataSetMember != null) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (!currDataSet.isBlank() && DsnUtil.isMember(command[1])) {
                        final var candidate = currDataSet + "(" + command[1] + ")";
                        terminal.printf("Are you sure you want to delete " + candidate + " y/n");
                    } else if (currDataSet.isBlank() && dataSetMember != null) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (currDataSet.isBlank() && DsnUtil.isDataSet(command[1])) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (!currDataSet.isBlank() && ("*".equals(command[1]) || ".".equals(command[1]))) {
                        terminal.printf("Are you sure you want to delete all from " + currDataSet + " y/n");
                    } else if (currDataSet.isBlank()) {
                        terminal.println(Constants.DATASET_NOT_SPECIFIED);
                        break;
                    } else {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    }

                    commandLine = textIO.newStringInputReader().withMaxLength(80).read("?");
                    if ("y".equalsIgnoreCase(commandLine) || "yes".equalsIgnoreCase(commandLine)) {
                        doIt = true;
                        break;
                    } else if ("n".equalsIgnoreCase(commandLine) || "no".equalsIgnoreCase(commandLine)) {
                        terminal.println("delete canceled");
                        break;
                    }
                } while (true);
                if (!doIt) {
                    continue;
                }
            }
            executeCommand(history.filterCommand(PromptUtil.getPrompt(), command));
        }

        textIO.dispose();
    }

    private static String[] removePrompt(String[] command) {
        LOG.debug("*** removePrompt ***");
        if (Constants.DEFAULT_PROMPT.equals(command[0])) {
            int size = command.length;
            String[] newCmdArr = new String[size - 1];
            List<String> newCmdLst = new ArrayList<>(Arrays.asList(command).subList(1, size));
            for (var i = 0; i < newCmdLst.size(); i++) {
                newCmdArr[i] = newCmdLst.get(i);
            }
            command = newCmdArr;
        }
        return command;
    }

    private String[] exclamationMark(String[] command) {
        LOG.debug("*** exclamationMark ***");
        if (command[0].startsWith("!")) {
            var str = new StringBuilder();
            for (var i = 0; i < command.length; i++) {
                str.append(command[i]);
                if (i + 1 != command.length) {
                    str.append(" ");
                }
            }

            var cmd = str.toString();
            if (cmd.length() == 1) {
                terminal.println(Constants.MISSING_PARAMETERS);
                return null;
            }

            var subStr = cmd.substring(1);
            boolean isStrNum = StrUtil.isStrNum(subStr);

            String newCmd;
            if ("!".equals(subStr)) {
                newCmd = history.getLastHistory();
            } else if (isStrNum) {
                newCmd = history.getHistoryByIndex(Integer.parseInt(subStr) - 1);
            } else {
                newCmd = history.getLastHistoryByValue(subStr);
            }

            // set new command from history content
            command = newCmd != null ? newCmd.split(" ") : null;
        }
        return command;
    }

    private static void executeCommand(String[] params) {
        LOG.debug("*** executeCommand ***");
        if (params.length == 0) {
            return;
        }
        var command = params[0];
        String param;
        history.addHistory(params);

        switch (command.toLowerCase()) {
            case "b":
            case "browse":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                if (params.length == 3 && !"all".equalsIgnoreCase(params[2])) {
                    terminal.println(Constants.INVALID_PARAMETER);
                    return;
                }
                var jobGet = new JobGet(currConnection);
                var browseJobService = new BrowseLogService(jobGet, params.length == 3, timeout);
                var browseJobController = new BrowseJobController(browseJobService);
                String browseJobResult = browseJobController.browseJob(params[1]);
                terminal.println(browseJobResult);
                commandOutput = new SearchCache("browsejob", new StringBuilder(browseJobResult));
                break;
            case "cancel":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var issueConsole = new IssueConsole(currConnection);
                var cancelService = new TerminateService(issueConsole, timeout);
                var cancelController = new CancelController(cancelService);
                String cancelResult = cancelController.cancel(params[1]);
                terminal.println(cancelResult);
                break;
            case "cat":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var dsnGet = new DsnGet(currConnection);
                var download = new Download(dsnGet, false);
                var concatService = new ConcatService(download, timeout);
                var concatController = new ConcatController(concatService);
                String concatResult = concatController.cat(currDataSet, params[1]);
                terminal.println(concatResult);
                commandOutput = new SearchCache("cat", new StringBuilder(concatResult));
                break;
            case "cd":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var dsnList = new DsnList(currConnection);
                var changeDirService = new ChangeDirService(dsnList);
                var changeDirController = new ChangeDirController(changeDirService);
                ResponseStatus responseStatus = changeDirController.cd(currDataSet, params[1].toUpperCase());
                if (responseStatus.isStatus()) {
                    currDataSet = responseStatus.getOptionalData();
                    terminal.println("set to " + currDataSet);
                } else {
                    terminal.println(responseStatus.getMessage());
                    terminal.println("set to " + currDataSet);
                }
                if (currDataSet.length() > currDataSetMax) {
                    currDataSetMax = currDataSet.length();
                }
                addVisited();
                break;
            case "change":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var changeConnService = new ChangeConnService(terminal);
                var changeConnController = new ChangeConnController(changeConnService);
                currConnection = changeConnController.changeZosConnection(currConnection, params);
                currSshConnection = changeConnController.changeSshConnection(currSshConnection, params);
                var msg = "Connected to " + currConnection.getHost() + " with user " + currConnection.getUser() + ".";
                terminal.println(msg);
                mainTerminal.setPaneTitle(Constants.APP_TITLE + " - " + currConnection.getHost().toUpperCase());
                break;
            case "clear":
                terminal.println();
                terminal.resetToBookmark("top");
                if (commandOutput != null) {
                    commandOutput.getOutput().setLength(0);
                    commandOutput = null;
                    System.gc();
                }
                terminal.println();
                break;
            case "color":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                if (params.length == 3) {
                    commands.color(params[1], params[2]);
                } else {
                    commands.color(params[1], null);
                }
                break;
            case "connections":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                changeConnService = new ChangeConnService(terminal);
                changeConnController = new ChangeConnController(changeConnService);
                changeConnController.displayConnections();
                break;
            case "cp":
            case "copy":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsMissing(2, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                var copyService = new CopyService(currConnection, timeout);
                var copyController = new CopyController(copyService);
                String copyResult = copyController.copy(currDataSet, params);
                terminal.println(copyResult);
                break;
            case "count":
                if (params.length == 1) {
                    terminal.println(Constants.MISSING_COUNT_PARAM);
                    return;
                }
                if (!("members".equalsIgnoreCase(params[1]) || "datasets".equalsIgnoreCase(params[1]))) {
                    terminal.println(Constants.MISSING_COUNT_PARAM);
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                dsnList = new DsnList(currConnection);
                var countService = new CountService(dsnList, timeout);
                var countController = new CountController(countService);
                String countResult = countController.count(currDataSet, params[1]);
                terminal.println(countResult);
                break;
            case "d":
            case "download":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                boolean isBinary = false;
                if (params.length == 3) {
                    if (params[2].equalsIgnoreCase("-b")) {
                        isBinary = true;
                    } else {
                        terminal.println(Constants.INVALID_PARAMETER);
                        return;
                    }
                }
                var downloadDsnService = new DownloadDsnService(currConnection, isBinary, timeout);
                var downloadDsnController = new DownloadDsnController(downloadDsnService);
                List<String> downloadResults = downloadDsnController.download(currDataSet, params[1]);
                downloadResults.forEach(terminal::println);
                break;
            case "dj":
            case "downloadjob":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                param = params[1];
                boolean isAll = false;
                if (params.length == 3) {
                    if (!"all".equalsIgnoreCase(params[2])) {
                        terminal.println(Constants.INVALID_PARAMETER);
                        return;
                    }
                    isAll = true;
                }
                jobGet = new JobGet(currConnection);
                var downloadJobService = new DownloadJobService(jobGet, isAll, timeout);
                var downloadJobController = new DownloadJobController(downloadJobService);
                String downloadJobResult = downloadJobController.downloadJob(param);
                terminal.println(downloadJobResult);
                break;
            case "end":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                break;
            case "env":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                commandOutput = commands.env();
                break;
            case "files":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                commandOutput = commands.files(currDataSet);
                break;
            case "g":
            case "grep":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsMissing(2, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                var grepService = new GrepService(currConnection, params[1], timeout);
                var grepController = new GrepController(grepService);
                String grepResult = grepController.grep(params[2], currDataSet);
                terminal.println(grepResult);
                break;
            case "h":
            case "help":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                if (params.length == 1) {
                    commandOutput = HelpService.display(terminal);
                }
                break;
            case "history":
                if (isParamsExceeded(2, params)) {
                    return;
                }
                if (params.length == 1) {
                    history.displayHistory();
                } else {
                    history.displayHistory(params[1]);
                }
                break;
            case "hostname":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                terminal.println(currConnection.getHost());
                return;
            case "ls":
                if (isParamsExceeded(3, params)) {
                    return;
                }
                dsnList = new DsnList(currConnection);
                var listingService = new ListingService(terminal, dsnList, timeout);
                var listingController = new ListingController(listingService);
                if (params.length == 3 && ("-l".equals(params[1]) || "--l".equals(params[1]))) {
                    boolean isAttributes = !"--l".equals(params[1]);
                    var value = params[2];
                    int size = params[2].length();
                    if (size <= 9 && value.charAt(size - 1) == '*') {  // is member with wild card specified...
                        if (isCurrDataSetNotSpecified()) {
                            return;
                        }
                        int index = value.indexOf("*");
                        var member = value.substring(0, index);
                        if (DsnUtil.isMember(member)) {  // validate member value without wild card char...
                            responseStatus = listingController.lsl(value, currDataSet, isAttributes);
                            if (!responseStatus.isStatus()) {
                                terminal.println(responseStatus.getMessage());
                            }
                        } else {
                            terminal.println(Constants.INVALID_MEMBER);
                        }
                        return;
                    } else if (DsnUtil.isMember(value)) {  // is member without wild card specified...
                        if (isCurrDataSetNotSpecified()) {
                            return;
                        }
                        responseStatus = listingController.lsl(value, currDataSet, isAttributes);
                        if (!responseStatus.isStatus()) {
                            terminal.println(responseStatus.getMessage());
                        }
                        return;
                    } else if (DsnUtil.isDataSet(value)) {  // is dataset specified at this point...
                        responseStatus = listingController.lsl(null, value, isAttributes);
                        if (!responseStatus.isStatus()) {
                            terminal.println(responseStatus.getMessage());
                        }
                        return;
                    } else {  // must be an invalid member or dataset specified...
                        terminal.println(Constants.INVALID_DATASET_AND_MEMBER);
                        return;
                    }
                }
                if (params.length == 2 && ("-l".equals(params[1]) || "--l".equals(params[1]))) {
                    if (isCurrDataSetNotSpecified()) {
                        return;
                    }
                    boolean isAttributes = !"--l".equals(params[1]);
                    responseStatus = listingController.lsl(currDataSet, isAttributes);
                    if (!responseStatus.isStatus()) {
                        terminal.println(responseStatus.getMessage());
                    }
                    addVisited();
                    return;
                }
                if (params.length == 2 && DsnUtil.isDataSet(params[1])) {
                    responseStatus = listingController.ls(params[1]);
                    if (!responseStatus.isStatus()) {
                        terminal.println(responseStatus.getMessage());
                    }
                    return;
                }
                if (isCurrDataSetNotSpecified()) {
                    return;
                }
                if (params.length == 2 && (params[1].length() <= 9 && params[1].charAt(params[1].length() - 1) == '*')) {
                    var value = params[1];
                    int index = value.indexOf("*");
                    var member = value.substring(0, index);
                    if (DsnUtil.isMember(member)) {
                        responseStatus = listingController.ls(value, currDataSet);
                        if (!responseStatus.isStatus()) {
                            terminal.println(responseStatus.getMessage());
                        }
                        return;
                    }
                }
                if (params.length == 2 && DsnUtil.isMember(params[1])) {
                    responseStatus = listingController.ls(params[1], currDataSet);
                    if (!responseStatus.isStatus()) {
                        terminal.println(responseStatus.getMessage());
                    }
                    return;
                }
                if (params.length == 2) {
                    terminal.println(Constants.INVALID_DATASET_AND_MEMBER);
                    return;
                }
                if (params.length == 1) {
                    responseStatus = listingController.ls(currDataSet);
                    if (!responseStatus.isStatus()) {
                        terminal.println(responseStatus.getMessage());
                    }
                    return;
                }
                // not valid input
                terminal.println(Constants.INVALID_ARGUMENTS);
                break;
            case "mkdir":
                if (isParamsMissing(1, params)) {
                    return;
                }
                disableKeys = true;
                commands.mkdir(currConnection, mainTextIO, currDataSet, params[1]);
                disableKeys = false;
                break;
            case "mvs":
                if (isParamsMissing(1, params)) {
                    return;
                }
                StringBuilder mvsCommandCandidate = getCommandFromParams(params);
                long mvsCommandCount = mvsCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(mvsCommandCount, mvsCommandCandidate)) {
                    var consoleService = new ConsoleService(currConnection, timeout);
                    var consoleController = new ConsoleController(consoleService);
                    String result = consoleController.issueConsole(mvsCommandCandidate.toString());
                    terminal.println(result);
                    commandOutput = new SearchCache("mvs", new StringBuilder(result));
                }
                break;
            case "ps":
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var processLstService = new ProcessLstService(new JobGet(currConnection), timeout);
                var processLstController = new ProcessLstController(processLstService);
                String result = "";
                if (params.length > 1) {
                    result = processLstController.processList(params[1]);
                    terminal.println(result);
                } else {
                    result = processLstController.processList();
                }
                commandOutput = new SearchCache("ps", new StringBuilder(result));
                break;
            case "p":
            case "purge":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var jobDelete = new JobDelete(currConnection);
                jobGet = new JobGet(currConnection);
                var purgeService = new PurgeService(jobDelete, jobGet, timeout);
                var purgeController = new PurgeController(purgeService);
                String purgeResult = purgeController.purge(params[1].toUpperCase());
                terminal.println(purgeResult);
                break;
            case "pwd":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                if (isCurrDataSetNotSpecified()) {
                    return;
                }
                terminal.println(currDataSet);
                break;
            case "rm":
                var deleteService = new DeleteService(currConnection, timeout);
                var deleteController = new DeleteController(deleteService);
                String deleteResult = deleteController.rm(currDataSet, params[1]);
                terminal.println(deleteResult);
                break;
            case "save":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var saveService = new SaveService(new DsnWrite(currConnection), timeout);
                var saveController = new SaveController(saveService);
                String saveResult = saveController.save(currDataSet, params[1]);
                terminal.println(saveResult);
                break;
            case "search":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                commands.search(commandOutput, params[1]);
                break;
            case "set":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                param = params[1];
                commands.set(param);
                break;
            case "stop":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                issueConsole = new IssueConsole(currConnection);
                var stopService = new TerminateService(issueConsole, timeout);
                var stopController = new StopController(stopService);
                String terminateResult = stopController.stop(params[1]);
                terminal.println(terminateResult);
                break;
            case "submit":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                if (isCurrDataSetNotSpecified()) {
                    return;
                }
                param = params[1];
                var jobSubmit = new JobSubmit(currConnection);
                var submitService = new SubmitService(jobSubmit, timeout);
                var submitController = new SubmitController(submitService);
                String submitResult = submitController.submit(currDataSet, param);
                terminal.println(submitResult);
                break;
            case "tail":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(4, params)) {
                    return;
                }
                jobGet = new JobGet(currConnection);
                var tailService = new TailService(terminal, jobGet, timeout);
                var tailController = new TailController(tailService);
                commandOutput = tailController.tail(params);
                break;
            case "t":
            case "timeout":
                if (isParamsExceeded(2, params)) {
                    return;
                }
                if (params.length == 1) {
                    LOG.debug("*** timeout display ***");
                    terminal.println("timeout value is " + timeout + " seconds.");
                } else {
                    try {
                        LOG.debug("*** timeout set value ***");
                        timeout = Long.parseLong(params[1]);
                        terminal.println("timeout value set to " + timeout + " seconds.");
                    } catch (NumberFormatException e) {
                        terminal.println(Constants.INVALID_VALUE);
                    }
                }
                break;
            case "touch":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var dsnWrite = new DsnWrite(currConnection);
                dsnList = new DsnList(currConnection);
                var touchService = new TouchService(dsnWrite, dsnList, timeout);
                var touchController = new TouchController(touchService);
                String touchResult = touchController.touch(currDataSet, params[1]);
                terminal.println(touchResult);
                break;
            case "tso":
                if (isParamsMissing(1, params)) {
                    return;
                }
                String acctNum = EnvVariableService.getInstance().getValueByKeyName("ACCTNUM");
                StringBuilder tsoCommandCandidate = getCommandFromParams(params);
                long tsoCommandCount = tsoCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(tsoCommandCount, tsoCommandCandidate)) {
                    var issueTso = new IssueTso(currConnection);
                    var tsoService = new TsoService(issueTso, acctNum, timeout);
                    var tsoController = new TsoController(tsoService);
                    String tsoResult = tsoController.issueCommand(acctNum, tsoCommandCandidate.toString());
                    terminal.println(tsoResult);
                }
                break;
            case "uname":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                var consoleService = new ConsoleService(currConnection, timeout);
                var unameController = new UnameController(consoleService);
                String unameResult = unameController.uname(currConnection);
                terminal.println(unameResult);
                break;
            case "ussh":
                if (isParamsMissing(1, params)) {
                    return;
                }
                StringBuilder ussCommandCandidate = getCommandFromParams(params);
                long ussCommandCount = ussCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(ussCommandCount, ussCommandCandidate)) {
                    var sshService = new SshService(currSshConnection);
                    var ussController = new UssController(sshService);
                    String ussResult = ussController.issueUnixCommand(ussCommandCandidate.toString());
                    terminal.println(ussResult);
                }
                break;
            case "v":
            case "visited":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                if (isCurrDataSetNotSpecified()) {
                    return;
                }
                for (final String key : dataSets.keySet()) {
                    List<String> lst = dataSets.get(key);
                    lst.forEach(l -> terminal.println(
                            Strings.padStart(l.toUpperCase(), currDataSetMax, ' ') + Constants.ARROW + key));
                }
                break;
            case "vi":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                dsnGet = new DsnGet(currConnection);
                download = new Download(dsnGet, false);
                var editService = new EditService(download, timeout);
                var editController = new EditController(editService);
                String editResult = editController.edit(currDataSet, params[1]);
                terminal.println(editResult);
                break;
            case "whoami":
                if (currConnection != null) {
                    terminal.println(currConnection.getUser());
                }
                break;
            default:
                terminal.println(Constants.INVALID_COMMAND);
        }
    }

    private static boolean isCommandValid(long count, StringBuilder commandCandidate) {
        LOG.debug("*** isCommandValid ***");
        if (count == 2 && commandCandidate.charAt(commandCandidate.length() - 1) == '\"') {
            return true;
        } else if (count == 2) {
            terminal.println(Constants.COMMAND_EXTRA_TEXT_INVALID_COMMAND);
        } else {
            terminal.println(Constants.COMMAND_INVALID_COMMAND);
        }
        return false;
    }

    private static StringBuilder getCommandFromParams(String[] params) {
        LOG.debug("*** getCommandFromParams ***");
        var command = new StringBuilder();
        for (var i = 1; i < params.length; i++) {
            command.append(params[i]);
            if (i != params.length - 1) {
                command.append(" ");
            }
        }
        return command;
    }

    private static void addVisited() {
        LOG.debug("*** addVisited ***");
        // if hostname and dataset not in datasets multimap add it
        if (currConnection == null || currConnection.getHost() == null && currDataSet.isBlank()) {
            return;
        }
        if (!dataSets.containsEntry(currConnection.getHost(), currDataSet)) {
            dataSets.put(currConnection.getHost(), currDataSet);
        }
    }

    private static boolean isParamsExceeded(int num, String[] params) {
        LOG.debug("*** isParamsExceeded ***");
        if (params.length > num) {
            terminal.println(Constants.TOO_MANY_PARAMETERS);
            return true;
        }
        return false;
    }

    private static boolean isParamsMissing(int num, String[] params) {
        LOG.debug("*** isParamsMissing ***");
        if (params.length == num) {
            terminal.println(Constants.MISSING_PARAMETERS);
            return true;
        }
        return false;
    }

    private static boolean isCurrDataSetNotSpecified() {
        LOG.debug("*** isCurrDataSetNotSpecified ***");
        if (currDataSet.isBlank()) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return true;
        }
        return false;
    }

}
