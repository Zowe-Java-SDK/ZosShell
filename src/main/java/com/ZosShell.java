package com;

import com.commands.Commands;
import com.commands.History;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.log.JobLog;
import com.security.Credentials;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final ListMultimap<String, String> dataSets = ArrayListMultimap.create();
    private static String currDataSet = "";
    private static final List<ZOSConnection> connections = new ArrayList<>();
    private static ZOSConnection currConnection;
    private static TextTerminal<?> terminal;
    private static Commands commands;
    private static History history;
    private static JobLog jobLog;

    public static void main(String[] args) {
        Credentials.readCredentials(connections);
        if (!connections.isEmpty())
            currConnection = connections.get(0);
        SwingTextTerminal mainTerm = new SwingTextTerminal();
        mainTerm.init();
        setTerminalProperties(mainTerm);
        TextIO mainTextIO = new TextIO(mainTerm);
        new ZosShell().accept(mainTextIO, null);
    }

    private static void setTerminalProperties(SwingTextTerminal mainTerm) {
        mainTerm.setPaneTitle(Constants.APP_TITLE);
        mainTerm.registerHandler("ctrl C", t -> {
            t.getTextPane().copy();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerm.registerHandler("UP", t -> {
            history.listUpCommands(Util.getPrompt(currConnection));
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerm.registerHandler("DOWN", t -> {
            history.listDownCommands(Util.getPrompt(currConnection));
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        terminal = textIO.getTextTerminal();
        commands = new Commands(connections, terminal);
        history = new History(terminal);
        if (currConnection == null)
            terminal.println(Constants.NO_CONNECTIONS);
        else terminal.println("Connected to " + currConnection.getHost() + " with user " + currConnection.getUser());

        String[] command;
        String commandLine = "";
        while (!"end".equalsIgnoreCase(commandLine)) {
            var prompt = Util.getPrompt(currConnection);
            commandLine = textIO.newStringInputReader().withMaxLength(80).read(prompt);
            command = commandLine.split(" ");

            command = exclamationMark(command);
            if (command == null) continue;

            if ("rm".equals(command[0])) {
                terminal.printf("Are you sure you want to delete y/n");
                commandLine = textIO.newStringInputReader().withMaxLength(80).read("?");
                if (!"y".equalsIgnoreCase(commandLine) && !"yes".equalsIgnoreCase(commandLine)) {
                    terminal.println("delete canceled");
                    continue;
                }
            }
            executeCommand(history.filterCommand(prompt, command));
        }

        textIO.dispose();
    }

    private String[] exclamationMark(String[] command) {
        if (command[0].startsWith("!")) {
            if (isParamsExceeded(1, command))
                return null;
            String cmd = command[0];
            if (cmd.length() == 1) {
                terminal.println(Constants.MISSING_PARAMETERS);
                return null;
            }
            String str = cmd.substring(1);
            boolean isStrNum = Util.isStrNum(str);
            String newCmd;
            if (isStrNum)
                newCmd = history.getHistoryByIndex(Integer.parseInt(str) - 1);
            else newCmd = history.getLastHistoryByValue(str);
            if (newCmd == null) return null;
            // set new command from history content
            command = newCmd.split(" ");
        }
        return command;
    }

    private static void executeCommand(String[] params) {
        if (params.length == 0) return;
        final var command = params[0];
        String param;
        history.addHistory(params);

        switch (command.toLowerCase()) {
            case "cancel":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.cancel(currConnection, param);
                break;
            case "cat":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.cat(currConnection, currDataSet, param);
                break;
            case "cd":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                currDataSet = commands.cd(currConnection, currDataSet, params[1].toUpperCase());
                if (currConnection != null && !currDataSet.isEmpty())
                    dataSets.put(currConnection.getHost(), currDataSet);
                if (!currDataSet.isEmpty()) terminal.println("set to " + currDataSet);
                break;
            case "change":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                currConnection = commands.change(currConnection, params);
                break;
            case "connections":
                commands.connections(currConnection);
                break;
            case "cp":
            case "copy":
                if (isParamsMissing(1, params))
                    return;
                if (params.length < 3)
                    return;
                if (isParamsExceeded(3, params))
                    return;
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
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.count(currConnection, currDataSet, param);
                break;
            case "download":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.download(currConnection, currDataSet, param);
                break;
            case "end":
                break;
            case "files":
                if (isParamsExceeded(1, params))
                    return;
                commands.files();
                break;
            case "get":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                jobLog = commands.get(currConnection, params);
                break;
            case "getall":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                jobLog = commands.getAll(currConnection, params, true);
                break;
            case "history":
                if (isParamsExceeded(2, params))
                    return;
                if (params.length == 1)
                    history.displayHistory();
                else history.displayHistory(params[1]);
                break;
            case "ls":
                if (isParamsExceeded(3, params))
                    return;
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
                    if (isCurrDataSetNotSpecified())
                        return;
                    commands.lsl(currConnection, currDataSet);
                    return;
                }
                if (isCurrDataSetNotSpecified())
                    return;
                commands.ls(currConnection, currDataSet);
                break;
            case "ps":
                if (isParamsExceeded(2, params))
                    return;
                if (params.length > 1) {
                    commands.ps(currConnection, params[1]);
                } else {
                    commands.ps(currConnection);
                }
                break;
            case "pwd":
                if (isParamsExceeded(1, params))
                    return;
                if (isCurrDataSetNotSpecified())
                    return;
                terminal.println(currDataSet);
                break;
            case "rm":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.rm(currConnection, currDataSet, param);
                break;
            case "save":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                commands.save(currConnection, currDataSet, params);
                break;
            case "search":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                commands.search(jobLog, params[1]);
                break;
            case "stop":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.stop(currConnection, param);
                break;
            case "submit":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                if (isCurrDataSetNotSpecified())
                    return;
                param = params[1];
                commands.submit(currConnection, currDataSet, param);
                break;
            case "tail":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(3, params))
                    return;
                commands.tail(currConnection, params);
                break;
            case "tailall":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(3, params))
                    return;
                commands.tailAll(currConnection, params, true);
                break;
            case "touch":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
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
                if (isParamsExceeded(1, params))
                    return;
                if (isCurrDataSetNotSpecified())
                    return;
                for (String key : dataSets.keySet()) {
                    List<String> lst = dataSets.get(key);
                    lst.forEach(l -> terminal.println(l.toUpperCase() + " ==> " + key));
                }
                break;
            case "vi":
                if (isParamsMissing(1, params))
                    return;
                if (isParamsExceeded(2, params))
                    return;
                commands.vi(currConnection, currDataSet, params);
                break;
            case "whoami":
                if (connections != null)
                    terminal.println(currConnection.getUser());
                break;
            default:
                terminal.println(Constants.INVALID_COMMAND);
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
