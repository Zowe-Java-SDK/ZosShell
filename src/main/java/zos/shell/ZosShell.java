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
import zos.shell.controller.container.ControllerFactoryContainer;
import zos.shell.record.DataSetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.autocomplete.SearchCommandService;
import zos.shell.service.help.HelpService;
import zos.shell.service.history.HistoryService;
import zos.shell.service.search.SearchCache;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.PromptUtil;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

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
    private static final SwingTextTerminal mainTerminal = new SwingTextTerminal();
    private static final SearchCommandService searchCommandService = new SearchCommandService();
    private static final ControllerFactoryContainer controllerContainer = new ControllerFactoryContainer();
    private static SshConnection currSshConnection;
    private static ZosConnection currConnection;
    private static TextTerminal<?> terminal;
    private static TextIO mainTextIO;
    private static final int defaultFontSize = 10;
    private static int fontSize = defaultFontSize;
    private static boolean fontSizeChanged = false;
    private static boolean disableKeys = false;
    private static String currDataset = "";
    private static int currDatasetMax = 0;
    private static HistoryService history;
    private static SearchCache searchCache;
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
                    var dataSetMember = DataSetMember.getDatasetAndMember(command[1]);
                    if (!currDataset.isBlank() && dataSetMember != null) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (!currDataset.isBlank() && DsnUtil.isMember(command[1])) {
                        var candidate = currDataset + "(" + command[1] + ")";
                        terminal.printf("Are you sure you want to delete " + candidate + " y/n");
                    } else if (currDataset.isBlank() && dataSetMember != null) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (currDataset.isBlank() && DsnUtil.isDataset(command[1])) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (!currDataset.isBlank() && ("*".equals(command[1]) || ".".equals(command[1]))) {
                        terminal.printf("Are you sure you want to delete all from " + currDataset + " y/n");
                    } else if (currDataset.isBlank()) {
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
        ResponseStatus responseStatus;
        String command = params[0];
        history.addHistory(params);

        switch (command.toLowerCase()) {
            case "bj":
            case "browsejob":
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
                var browseJobController = controllerContainer.getBrowseJobController(currConnection,
                        params.length == 3, timeout);
                String browseJobResult = browseJobController.browseJob(params[1]);
                terminal.println(browseJobResult);
                searchCache = new SearchCache("browsejob", new StringBuilder(browseJobResult));
                break;
            case "cancel":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var cancelController = controllerContainer.getCancelController(currConnection, timeout);
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
                var concatController = controllerContainer.getConcatController(currConnection, timeout);
                String concatResult = concatController.cat(currDataset, params[1]);
                terminal.println(concatResult);
                searchCache = new SearchCache("cat", new StringBuilder(concatResult));
                break;
            case "cd":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var changeDirController = controllerContainer.getChangeDirController(currConnection);
                responseStatus = changeDirController.cd(currDataset, params[1].toUpperCase());
                if (responseStatus.isStatus()) {
                    currDataset = responseStatus.getOptionalData();
                    terminal.println("set to " + currDataset);
                } else {
                    terminal.println(responseStatus.getMessage());
                    terminal.println("set to " + currDataset);
                }
                if (currDataset.length() > currDatasetMax) {
                    currDatasetMax = currDataset.length();
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
                var changeConnController = controllerContainer.getChangeConnController(terminal);
                currConnection = changeConnController.changeZosConnection(currConnection, params);
                currSshConnection = changeConnController.changeSshConnection(currSshConnection, params);
                var msg = "Connected to " + currConnection.getHost() + " with user " + currConnection.getUser() + ".";
                terminal.println(msg);
                mainTerminal.setPaneTitle(Constants.APP_TITLE + " - " + currConnection.getHost().toUpperCase());
                break;
            case "clear":
                terminal.println();
                terminal.resetToBookmark("top");
                if (searchCache != null) {
                    searchCache.getOutput().setLength(0);
                    searchCache = null;
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
                var changeWinController = controllerContainer.getChangeWinController(terminal);
                String colorResult;
                if (params.length == 3) {
                    colorResult = changeWinController.changeColorSettings(params[1], params[2]);
                } else {
                    colorResult = changeWinController.changeColorSettings(params[1], null);
                }
                terminal.println(colorResult);
                break;
            case "connections":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                controllerContainer.getChangeConnController(terminal).displayConnections();
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
                var copyController = controllerContainer.getCopyController(currConnection, timeout);
                String copyResult = copyController.copy(currDataset, params);
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
                var countController = controllerContainer.getCountController(currConnection, timeout);
                String countResult = countController.count(currDataset, params[1]);
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
                if (currDataset.isBlank()) {
                    terminal.println(Constants.DATASET_NOT_SPECIFIED);
                    break;
                }
                var downloadDsnController = controllerContainer.getDownloadDsnController(currConnection, isBinary, timeout);
                List<String> downloadResults = downloadDsnController.download(currDataset, params[1]);
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
                boolean isAll = false;
                if (params.length == 3) {
                    if (!"all".equalsIgnoreCase(params[2])) {
                        terminal.println(Constants.INVALID_PARAMETER);
                        return;
                    }
                    isAll = true;
                }
                var downloadJobController = controllerContainer.getDownloadJobController(currConnection, isAll, timeout);
                String downloadJobResult = downloadJobController.downloadJob(params[1]);
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
                String envResult = controllerContainer.getEnvVariableController().env();
                terminal.println(envResult);
                searchCache = new SearchCache("env", new StringBuilder(envResult));
                break;
            case "files":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                StringBuilder resultLocalFiles = controllerContainer.getLocalFilesController().files(currDataset);
                terminal.println(resultLocalFiles.toString());
                searchCache = new SearchCache("files", resultLocalFiles);
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
                var grepController = controllerContainer.getGrepController(currConnection, params[1], timeout);
                String grepResult = grepController.grep(params[2], currDataset);
                terminal.println(grepResult);
                break;
            case "h":
            case "help":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                if (params.length == 1) {
                    searchCache = HelpService.display(terminal);
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
                var listingController = controllerContainer.getListingController(currConnection, terminal, timeout);
                if (params.length == 3 && ("-l".equals(params[1]) || "--l".equals(params[1]))) {
                    boolean isAttributes = !"--l".equals(params[1]);
                    var value = params[2];
                    int size = params[2].length();
                    if (size <= 9 && value.charAt(size - 1) == '*') {  // is member with wild card specified...
                        if (iscurrDatasetNotSpecified()) {
                            return;
                        }
                        int index = value.indexOf("*");
                        var member = value.substring(0, index);
                        if (DsnUtil.isMember(member)) {  // validate member value without wild card char...
                            responseStatus = listingController.lsl(value, currDataset, isAttributes);
                            if (!responseStatus.isStatus()) {
                                terminal.println(responseStatus.getMessage());
                            }
                        } else {
                            terminal.println(Constants.INVALID_MEMBER);
                        }
                        return;
                    } else if (DsnUtil.isMember(value)) {  // is member without wild card specified...
                        if (iscurrDatasetNotSpecified()) {
                            return;
                        }
                        responseStatus = listingController.lsl(value, currDataset, isAttributes);
                        if (!responseStatus.isStatus()) {
                            terminal.println(responseStatus.getMessage());
                        }
                        return;
                    } else if (DsnUtil.isDataset(value)) {  // is dataset specified at this point...
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
                    if (iscurrDatasetNotSpecified()) {
                        return;
                    }
                    boolean isAttributes = !"--l".equals(params[1]);
                    responseStatus = listingController.lsl(currDataset, isAttributes);
                    if (!responseStatus.isStatus()) {
                        terminal.println(responseStatus.getMessage());
                    }
                    addVisited();
                    return;
                }
                if (params.length == 2 && DsnUtil.isDataset(params[1])) {
                    responseStatus = listingController.ls(params[1]);
                    if (!responseStatus.isStatus()) {
                        terminal.println(responseStatus.getMessage());
                    }
                    return;
                }
                if (iscurrDatasetNotSpecified()) {
                    return;
                }
                if (params.length == 2 && (params[1].length() <= 9 && params[1].charAt(params[1].length() - 1) == '*')) {
                    var value = params[1];
                    int index = value.indexOf("*");
                    var member = value.substring(0, index);
                    if (DsnUtil.isMember(member)) {
                        responseStatus = listingController.ls(value, currDataset);
                        if (!responseStatus.isStatus()) {
                            terminal.println(responseStatus.getMessage());
                        }
                        return;
                    }
                }
                if (params.length == 2 && DsnUtil.isMember(params[1])) {
                    responseStatus = listingController.ls(params[1], currDataset);
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
                    responseStatus = listingController.ls(currDataset);
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
                var makeDirController = controllerContainer.getMakeDirController(currConnection, terminal, timeout);
                makeDirController.mkdir(mainTextIO, currDataset, params[1]);
                disableKeys = false;
                break;
            case "mvs":
                if (isParamsMissing(1, params)) {
                    return;
                }
                StringBuilder mvsCommandCandidate = getCommandFromParams(params);
                long mvsCommandCount = mvsCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(mvsCommandCount, mvsCommandCandidate)) {
                    var consoleController = controllerContainer.getConsoleController(currConnection, timeout);
                    String result = consoleController.issueConsole(mvsCommandCandidate.toString());
                    terminal.println(result);
                    searchCache = new SearchCache("mvs", new StringBuilder(result));
                }
                break;
            case "ps":
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var processLstController = controllerContainer.getProcessLstController(currConnection, timeout);
                String result;
                if (params.length > 1) {
                    result = processLstController.processList(params[1]);
                } else {
                    result = processLstController.processList();
                }
                terminal.println(result);
                searchCache = new SearchCache("ps", new StringBuilder(result));
                break;
            case "p":
            case "purge":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var purgeController = controllerContainer.getPurgeController(currConnection, timeout);
                String purgeResult = purgeController.purge(params[1].toUpperCase());
                terminal.println(purgeResult);
                break;
            case "pwd":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                if (iscurrDatasetNotSpecified()) {
                    return;
                }
                terminal.println(currDataset);
                break;
            case "rm":
                var deleteController = controllerContainer.getDeleteController(currConnection, timeout);
                String deleteResult = deleteController.rm(currDataset, params[1]);
                terminal.println(deleteResult);
                break;
            case "save":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var saveController = controllerContainer.getSaveController(currConnection, timeout);
                String saveResult = saveController.save(currDataset, params[1]);
                terminal.println(saveResult);
                break;
            case "search":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var searchCacheController = controllerContainer.getSearchCacheController();
                searchCacheController.search(searchCache, params[1]).forEach(terminal::println);
                break;
            case "set":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                String setResult = controllerContainer.getEnvVariableController().set(params[1]);
                terminal.println(setResult);
                break;
            case "stop":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var stopController = controllerContainer.getStopController(currConnection, timeout);
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
                if (iscurrDatasetNotSpecified()) {
                    return;
                }
                var submitController = controllerContainer.getSubmitController(currConnection, timeout);
                String submitResult = submitController.submit(currDataset, params[1]);
                terminal.println(submitResult);
                break;
            case "tail":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(4, params)) {
                    return;
                }
                var tailController = controllerContainer.getTailController(currConnection, terminal, timeout);
                searchCache = tailController.tail(params);
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
                var touchController = controllerContainer.getTouchController(currConnection, timeout);
                String touchResult = touchController.touch(currDataset, params[1]);
                terminal.println(touchResult);
                break;
            case "tso":
                if (isParamsMissing(1, params)) {
                    return;
                }
                String acctNum = controllerContainer.getEnvVariableController().getValueByEnv("ACCTNUM");
                StringBuilder tsoCommandCandidate = getCommandFromParams(params);
                long tsoCommandCount = tsoCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(tsoCommandCount, tsoCommandCandidate)) {
                    var tsoController = controllerContainer.getTsoController(currConnection, acctNum, timeout);
                    String tsoResult = tsoController.issueCommand(acctNum, tsoCommandCandidate.toString());
                    searchCache = new SearchCache("tso", new StringBuilder(tsoResult));
                    terminal.println(tsoResult);
                }
                break;
            case "uname":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                var unameController = controllerContainer.getUnameController(currConnection, timeout);
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
                    var ussController = controllerContainer.getUssController(currSshConnection);
                    String ussResult = ussController.issueUnixCommand(ussCommandCandidate.toString());
                    terminal.println(ussResult);
                }
                break;
            case "v":
            case "visited":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                if (iscurrDatasetNotSpecified()) {
                    return;
                }
                for (final String key : dataSets.keySet()) {
                    List<String> lst = dataSets.get(key);
                    lst.forEach(l -> terminal.println(
                            Strings.padStart(l.toUpperCase(), currDatasetMax, ' ') + Constants.ARROW + key));
                }
                break;
            case "vi":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var editController = controllerContainer.getEditController(currConnection, timeout);
                String editResult = editController.edit(currDataset, params[1]);
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
        if (currConnection == null || currConnection.getHost() == null && currDataset.isBlank()) {
            return;
        }
        if (!dataSets.containsEntry(currConnection.getHost(), currDataset)) {
            dataSets.put(currConnection.getHost(), currDataset);
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

    private static boolean iscurrDatasetNotSpecified() {
        LOG.debug("*** iscurrDatasetNotSpecified ***");
        if (currDataset.isBlank()) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return true;
        }
        return false;
    }

}
