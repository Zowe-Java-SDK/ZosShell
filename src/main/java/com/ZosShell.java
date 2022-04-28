package com;

import com.commands.Commands;
import com.commands.History;
import com.config.ColorConfig;
import com.config.Credentials;
import com.dto.JobOutput;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.utility.Util;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;
import zowe.client.sdk.core.ZOSConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final ListMultimap<String, String> dataSets = ArrayListMultimap.create();
    private static String currDataSet = "";
    private static int currDataSetMax = 0;
    private static final List<ZOSConnection> connections = new ArrayList<>();
    private static ZOSConnection currConnection;
    private static TextTerminal<?> terminal;
    private static Commands commands;
    private static History history;
    private static JobOutput jobOutput;
    private static final SwingTextTerminal mainTerminal = new SwingTextTerminal();

    public static void main(String[] args) {
        Credentials.readCredentials(connections);
        if (!connections.isEmpty()) {
            currConnection = connections.get(0);
        }
        mainTerminal.init();
        setTerminalProperties();
        var mainTextIO = new TextIO(mainTerminal);
        new ZosShell().accept(mainTextIO, null);
    }

    private static void setTerminalProperties() {
        mainTerminal.setPaneTitle(Constants.APP_TITLE + " - " + currConnection.getHost().toUpperCase());
        mainTerminal.registerHandler("ctrl C", t -> {
            t.getTextPane().copy();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("UP", t -> {
            history.listUpCommands(Util.getPrompt());
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("DOWN", t -> {
            history.listDownCommands(Util.getPrompt());
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        terminal = textIO.getTextTerminal();
        terminal.setBookmark("top");
        ColorConfig.readConfig(terminal);
        commands = new Commands(connections, terminal, mainTerminal);
        history = new History(terminal);
        if (currConnection == null) {
            terminal.println(Constants.NO_CONNECTIONS);
        } else {
            terminal.println("Connected to " + currConnection.getHost() + " with user " + currConnection.getUser());
        }

        String[] command;
        String commandLine = "";
        while (!"end".equalsIgnoreCase(commandLine)) {
            commandLine = textIO.newStringInputReader().withMaxLength(80).read(Util.getPrompt());
            command = commandLine.split(" ");

            command = exclamationMark(command);
            if (command == null) {
                continue;
            }

            if ("rm".equals(command[0])) {
                terminal.printf("Are you sure you want to delete y/n");
                commandLine = textIO.newStringInputReader().withMaxLength(80).read("?");
                if (!"y".equalsIgnoreCase(commandLine) && !"yes".equalsIgnoreCase(commandLine)) {
                    terminal.println("delete canceled");
                    continue;
                }
            }
            executeCommand(history.filterCommand(Util.getPrompt(), command));
        }

        textIO.dispose();
    }

    private String[] exclamationMark(String[] command) {
        if (command[0].startsWith("!")) {
            if (isParamsExceeded(1, command)) {
                return null;
            }
            var cmd = command[0];
            if (cmd.length() == 1) {
                terminal.println(Constants.MISSING_PARAMETERS);
                return null;
            }
            var str = cmd.substring(1);
            var isStrNum = Util.isStrNum(str);
            String newCmd;
            if ("!".equals(str)) {
                newCmd = history.getLastHistory();
            } else if (isStrNum) {
                newCmd = history.getHistoryByIndex(Integer.parseInt(str) - 1);
            } else {
                newCmd = history.getLastHistoryByValue(str);
            }
            if (newCmd == null) {
                return null;
            }
            // set new command from history content
            command = newCmd.split(" ");
        }
        return command;
    }

    private static void executeCommand(String[] params) {
        if (params.length == 0) {
            return;
        }
        final var command = params[0];
        String param;
        history.addHistory(params);

        switch (command.toLowerCase()) {
            case "browsejob":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                jobOutput = commands.browse(currConnection, params);
                break;
            case "cancel":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                param = params[1];
                commands.cancel(currConnection, param);
                break;
            case "cat":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                param = params[1];
                commands.cat(currConnection, currDataSet, param);
                break;
            case "cd":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                currDataSet = commands.cd(currConnection, currDataSet, params[1].toUpperCase());
                if (currDataSet.length() > currDataSetMax) {
                    currDataSetMax = currDataSet.length();
                }
                addVisited();
                if (!currDataSet.isEmpty()) {
                    terminal.println("set to " + currDataSet);
                }
                break;
            case "change":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                currConnection = commands.change(currConnection, params);
                mainTerminal.setPaneTitle(Constants.APP_TITLE + " - " + currConnection.getHost().toUpperCase());
                break;
            case "clearlog":
                if (jobOutput != null) {
                    jobOutput.getOutput().setLength(0);
                    jobOutput = null;
                    System.gc();
                }
                break;
            case "clear":
                terminal.resetToBookmark("top");
                break;
            case "color":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                commands.color(params[1]);
                break;
            case "connections":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                commands.connections(currConnection);
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
                commands.copy(currConnection, currDataSet, params);
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
                param = params[1];
                commands.count(currConnection, currDataSet, param);
                break;
            case "download":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                param = params[1];
                boolean isBinary = false;
                if (params.length == 3) {
                    if (params[2].equalsIgnoreCase("-b")) {
                        isBinary = true;
                    } else {
                        terminal.println(Constants.INVALID_PARAMETER);
                        return;
                    }
                }
                commands.download(currConnection, currDataSet, param, isBinary);
                break;
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
                commands.downloadJob(currConnection, param, isAll);
                break;
            case "end":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                break;
            case "files":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                commands.files(currDataSet);
                break;
            case "h":
            case "help":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                if (params.length == 1) {
                    commands.help();
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
                if (params.length == 2 && Util.isDataSet(params[1])) {
                    commands.ls(currConnection, params[1]);
                    return;
                }
                if (params.length == 2 && !"-l".equalsIgnoreCase(params[1])) {
                    terminal.println(Constants.INVALID_COMMAND);
                    return;
                }
                if (params.length == 3 && !"-l".equalsIgnoreCase(params[1])) {
                    terminal.println(Constants.INVALID_COMMAND);
                    return;
                }
                if (params.length == 3 && "-l".equalsIgnoreCase(params[1])) {
                    if (!Util.isDataSet(params[2])) {
                        terminal.println(Constants.INVALID_DATASET);
                        return;
                    }
                    commands.lsl(currConnection, params[2]);
                    return;
                }
                if (params.length == 2 && "-l".equalsIgnoreCase(params[1])) {
                    if (isCurrDataSetNotSpecified()) {
                        return;
                    }
                    commands.lsl(currConnection, currDataSet);
                    addVisited();
                    return;
                }
                if (isCurrDataSetNotSpecified()) {
                    return;
                }
                commands.ls(currConnection, currDataSet);
                addVisited();
                break;
            case "mvs":
                if (isParamsMissing(1, params)) {
                    return;
                }
                StringBuilder mvsCommandCandidate = new StringBuilder();
                for (int i = 1; i < params.length; i++) {
                    mvsCommandCandidate.append(params[i]);
                    if (i != params.length - 1) {
                        mvsCommandCandidate.append(" ");
                    }
                }
                var count = mvsCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (count == 2 && mvsCommandCandidate.charAt(mvsCommandCandidate.length() - 1) == '\"') {
                    commands.mvsCommand(currConnection, mvsCommandCandidate.toString());
                } else if (count == 2) {
                    terminal.println(Constants.MVS_EXTRA_TEXT_INVALID_COMMAND);
                } else {
                    terminal.println(Constants.MVS_INVALID_COMMAND);
                }
                break;
            case "ps":
                if (isParamsExceeded(2, params)) {
                    return;
                }
                if (params.length > 1) {
                    commands.ps(currConnection, params[1]);
                } else {
                    commands.ps(currConnection);
                }
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
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                param = params[1];
                commands.rm(currConnection, currDataSet, param);
                break;
            case "save":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                commands.save(currConnection, currDataSet, params);
                break;
            case "search":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                commands.search(jobOutput, params[1]);
                break;
            case "stop":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                param = params[1];
                commands.stop(currConnection, param);
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
                commands.submit(currConnection, currDataSet, param);
                break;
            case "tailjob":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(4, params)) {
                    return;
                }
                JobOutput tailJobOutput = commands.tailJob(currConnection, params);
                if (tailJobOutput != null) {
                    jobOutput = tailJobOutput;
                }
                break;
            case "touch":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                commands.touch(currConnection, currDataSet, params);
                break;
            case "uname":
                if (currConnection != null) {
                    terminal.println(
                            "hostname: " + currConnection.getHost() + ", port: " + currConnection.getZosmfPort());
                } else {
                    terminal.println(Constants.NO_INFO);
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
                for (String key : dataSets.keySet()) {
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
                commands.vi(currConnection, currDataSet, params);
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

    private static void addVisited() {
        // if hostname and dataset not in datasets multimap add it
        if (currConnection == null || currConnection.getHost() == null && currDataSet.isEmpty()) {
            return;
        }
        if (!dataSets.containsEntry(currConnection.getHost(), currDataSet)) {
            dataSets.put(currConnection.getHost(), currDataSet);
        }
    }

    private static boolean isParamsExceeded(int num, String[] params) {
        if (params.length > num) {
            terminal.println(Constants.TOO_MANY_PARAMETERS);
            return true;
        }
        return false;
    }

    private static boolean isParamsMissing(int num, String[] params) {
        if (params.length == num) {
            terminal.println(Constants.MISSING_PARAMETERS);
            return true;
        }
        return false;
    }

    private static boolean isCurrDataSetNotSpecified() {
        if (currDataSet.isEmpty()) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return true;
        }
        return false;
    }

}
