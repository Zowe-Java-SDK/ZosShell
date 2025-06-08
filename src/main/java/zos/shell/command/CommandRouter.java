package zos.shell.command;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainer;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.help.HelpService;
import zos.shell.service.search.SearchCache;
import zos.shell.singleton.ConnSingleton;
import zos.shell.singleton.HistorySingleton;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.utility.DsnUtil;
import zowe.client.sdk.core.ZosConnection;

import java.util.List;

public class CommandRouter {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRouter.class);
    private static final ListMultimap<String, String> dataSets = ArrayListMultimap.create();
    private static final ControllerFactoryContainer controllerContainer = new ControllerFactoryContainer();
    private final TextTerminal<?> terminal;
    private long timeout = Constants.FUTURE_TIMEOUT_VALUE;
    private String currDataset = "";
    private int currDatasetMax = 0;
    private SearchCache searchCache;

    public CommandRouter(final TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void routeCommand(String[] params, String input) {
        LOG.debug("*** routeCommand ***");
        if (params == null || params.length == 0) {
            return;
        }

        var currConnection = ConnSingleton.getInstance().getCurrZosConnection();
        var currSshConnection = ConnSingleton.getInstance().getCurrSshConnection();
        ResponseStatus responseStatus;

        var command = params[0].trim();
        HistorySingleton.getInstance().getHistory().addHistory(params);
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
                var browseJobResult = browseJobController.browseJob(params[1]);
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
                var changeDirController = controllerContainer.getChangeDirController();
                responseStatus = changeDirController.cd(currDataset, params[1].toUpperCase());
                if (responseStatus.isStatus()) {
                    currDataset = responseStatus.getOptionalData();
                } else {
                    terminal.println(responseStatus.getMessage());
                }
                if (!currDataset.isBlank()) {
                    terminal.println("set to " + currDataset);
                }
                if (currDataset.length() > currDatasetMax) {
                    currDatasetMax = currDataset.length();
                }
                addVisited(currConnection, currDataset);
                break;
            case "change":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                int changeIndex;
                try {
                    changeIndex = Integer.parseInt(params[1]);
                } catch (NumberFormatException e) {
                    terminal.println(Constants.INVALID_COMMAND);
                    return;
                }
                var previousCurrConnection = currConnection;
                var changeConnController = controllerContainer.getChangeConnController(terminal);
                currConnection = changeConnController.changeZosConnection(currConnection, params);
                ConnSingleton.getInstance().setCurrZosConnection(currConnection, changeIndex);
                currSshConnection = changeConnController.changeSshConnection(currSshConnection, params);
                ConnSingleton.getInstance().setCurrSshConnection(currSshConnection);
                if (previousCurrConnection != currConnection) {
                    var msg = String.format("Connection changed:\nhost:%s\nuser:%s\nzosmfport:%s\nsshport:%s",
                            currConnection.getHost(), currConnection.getUser(), currConnection.getZosmfPort(),
                            currSshConnection.getPort());
                    terminal.println(msg);
                }
                TerminalSingleton.getInstance().getMainTerminal()
                        .setPaneTitle(Constants.APP_TITLE + " - " + currConnection.getHost().toUpperCase());
                break;
            case "cls":
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
            case "echo":
                if (isParamsMissing(1, params)) {
                    return;
                }
                var echoArg = input.substring(5);
                var echoController = controllerContainer.getEchoController();
                String echoResult = echoController.getEcho(echoArg);
                terminal.println(echoResult);
                break;
            case "exit":
            case "quit":
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
                if (isParamsExceeded(2, params)) {
                    return;
                }
                if (params.length == 1) {
                    searchCache = HelpService.display(terminal);
                } else if (params[1].equalsIgnoreCase("-l")) {
                    searchCache = HelpService.displayCommandNames(terminal);
                } else {
                    searchCache = HelpService.displayCommand(terminal, params[1]);
                    if (searchCache.getOutput().length() == 0) {
                        searchCache = HelpService.displayCommandAbbreviation(terminal, params[1]);
                    }
                }
                if (searchCache.getOutput().length() == 0) {
                    terminal.println(Constants.HELP_COMMAND_NOT_FOUND);
                }
                break;
            case "history":
                if (isParamsExceeded(2, params)) {
                    return;
                }
                if (params.length == 1) {
                    HistorySingleton.getInstance().getHistory().displayHistory();
                } else {
                    HistorySingleton.getInstance().getHistory().displayHistory(params[1]);
                }
                break;
            case "hostname":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                terminal.println(currConnection.getHost());
                break;
            case "ls":
                if (isParamsExceeded(3, params)) {
                    return;
                }
                var listingController = controllerContainer.getListingController(currConnection, terminal, timeout);
                if (params.length == 3 && ("-l".equals(params[1]) || "--l".equals(params[1]))) {
                    boolean isAttributes = !"--l".equals(params[1]);
                    var value = params[2];
                    int size = params[2].length();
                    if (size <= 9 && value.charAt(size - 1) == '*') {  // is a member with wild card specified...
                        if (isDatasetNotSpecified(currDataset)) {
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
                        if (isDatasetNotSpecified(currDataset)) {
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
                    if (isDatasetNotSpecified(currDataset)) {
                        return;
                    }
                    boolean isAttributes = !"--l".equals(params[1]);
                    responseStatus = listingController.lsl(currDataset, isAttributes);
                    if (!responseStatus.isStatus()) {
                        terminal.println(responseStatus.getMessage());
                    }
                    addVisited(currConnection, currDataset);
                    return;
                }
                if (params.length == 2 && DsnUtil.isDataset(params[1])) {
                    responseStatus = listingController.ls(params[1]);
                    if (!responseStatus.isStatus()) {
                        terminal.println(responseStatus.getMessage());
                    }
                    return;
                }
                if (isDatasetNotSpecified(currDataset)) {
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
                TerminalSingleton.getInstance().setDisableKeys(true);
                var makeDirController = controllerContainer.getMakeDirController(currConnection, terminal, timeout);
                makeDirController.mkdir(TerminalSingleton.getInstance().getMainTextIO(), currDataset, params[1]);
                TerminalSingleton.getInstance().setDisableKeys(false);
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
                if (isDatasetNotSpecified(currDataset)) {
                    return;
                }
                terminal.println(currDataset);
                break;
            case "rn":
            case "rename":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsMissing(2, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                var renameController = controllerContainer.getRenameController(currConnection, timeout);
                String renameResult = renameController.rename(currDataset, params[1], params[2]);
                terminal.println(renameResult);
                break;
            case "rm":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                boolean doIt = false;
                do {
                    var dataSetMember = DatasetMember.getDatasetAndMember(params[1]);
                    if (!currDataset.isBlank() && dataSetMember != null) {
                        terminal.printf("Are you sure you want to delete " + params[1] + " y/n");
                    } else if (!currDataset.isBlank() && DsnUtil.isMember(params[1])) {
                        var candidate = currDataset + "(" + params[1] + ")";
                        terminal.printf("Are you sure you want to delete " + candidate + " y/n");
                    } else if (currDataset.isBlank() && dataSetMember != null) {
                        terminal.printf("Are you sure you want to delete " + params[1] + " y/n");
                    } else if (currDataset.isBlank() && DsnUtil.isDataset(params[1])) {
                        terminal.printf("Are you sure you want to delete " + params[1] + " y/n");
                    } else if (!currDataset.isBlank() && ("*".equals(params[1]) || ".".equals(params[1]))) {
                        terminal.printf("Are you sure you want to delete all from " + currDataset + " y/n");
                    } else if (currDataset.isBlank()) {
                        terminal.println(Constants.DATASET_NOT_SPECIFIED);
                        break;
                    } else {
                        terminal.printf("Are you sure you want to delete " + params[1] + " y/n");
                    }

                    String commandLine = TerminalSingleton.getInstance()
                            .getMainTextIO().newStringInputReader().withMaxLength(80).read("?");
                    if ("y".equalsIgnoreCase(commandLine) || "yes".equalsIgnoreCase(commandLine)) {
                        doIt = true;
                        break;
                    } else if ("n".equalsIgnoreCase(commandLine) || "no".equalsIgnoreCase(commandLine)) {
                        terminal.println("delete canceled");
                        break;
                    }
                } while (true);
                if (!doIt) {
                    return;
                }
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
                if (!input.contains("=")) {
                    terminal.println(Constants.INVALID_COMMAND);
                    return;
                }
                if (new StringBuilder(input).codePoints().filter(ch -> ch == '=').count() > 2) {
                    terminal.println(Constants.INVALID_COMMAND);
                    return;
                }
                int index = input.indexOf("=");
                var setFirstHalfStr = input.substring(0, index + 1);
                if (!setFirstHalfStr.toUpperCase().startsWith("SET ")) {
                    terminal.println(Constants.INVALID_COMMAND);
                    return;
                }
                var setValuesLst = input.split(" ");
                var setKeyValueStr = new StringBuilder();
                for (int i = 1; i < setValuesLst.length; i++) {
                    if (i == setValuesLst.length - 1) {
                        setKeyValueStr.append(setValuesLst[i]);
                    } else {
                        setKeyValueStr.append(setValuesLst[i]).append(" ");
                    }
                }
                var envVariableController = controllerContainer.getEnvVariableController();
                String setResult = envVariableController.set(setKeyValueStr.toString());
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
                StringBuilder tsoCommandCandidate = getCommandFromParams(params);
                long tsoCommandCount = tsoCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(tsoCommandCount, tsoCommandCandidate)) {
                    var tsoController = controllerContainer.getTsoController(currConnection, timeout);
                    String tsoResult = tsoController.issueCommand(tsoCommandCandidate.toString());
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
            case "usermod":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                var usermodController = controllerContainer.getUsermodController(currConnection,
                        ConnSingleton.getInstance().getCurrZosConnectionIndex());
                var flag = params[1];
                String usermodResult = usermodController.change(flag);
                terminal.println(usermodResult);
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
                for (final String key : dataSets.keySet()) {
                    List<String> lst = dataSets.get(key);
                    lst.forEach(l -> terminal.println(
                            Strings.padStart(l.toUpperCase(), currDatasetMax, ' ') + Constants.ARROW + key));
                }
                if (dataSets.isEmpty()) {
                    terminal.println(Constants.NO_VISITED_DATASETS);
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

    private void addVisited(final ZosConnection connection, final String dataset) {
        LOG.debug("*** addVisited ***");
        // if valid hostname and dataset not in datasets multimap add it
        if (connection == null || connection.getHost() == null || dataset.isBlank()) {
            return;
        }
        if (!dataSets.containsEntry(connection.getHost(), dataset)) {
            dataSets.put(connection.getHost(), dataset);
        }
    }

    private StringBuilder getCommandFromParams(final String[] params) {
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

    private boolean isCommandValid(final long count, final StringBuilder cmd) {
        LOG.debug("*** isCommandValid ***");
        if (count == 2 && cmd.charAt(cmd.length() - 1) == '\"') {
            return true;
        } else if (count == 2) {
            terminal.println(Constants.COMMAND_EXTRA_TEXT_INVALID_COMMAND);
        } else {
            terminal.println(Constants.COMMAND_INVALID_COMMAND);
        }
        return false;
    }

    private boolean isDatasetNotSpecified(final String dataset) {
        LOG.debug("*** isDatasetNotSpecified ***");
        if (dataset.isBlank()) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return true;
        }
        return false;
    }

    private boolean isParamsExceeded(final int num, final String[] params) {
        LOG.debug("*** isParamsExceeded ***");
        if (params.length > num) {
            terminal.println(Constants.TOO_MANY_PARAMETERS);
            return true;
        }
        return false;
    }

    private boolean isParamsMissing(final int num, final String[] params) {
        LOG.debug("*** isParamsMissing ***");
        if (params.length == num) {
            terminal.println(Constants.MISSING_PARAMETERS);
            return true;
        }
        return false;
    }

}
