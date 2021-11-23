package com;

import com.command.Commands;
import com.credential.Credentials;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static ListMultimap<String, String> dataSets = ArrayListMultimap.create();
    private static List<String> commandLst = new LinkedList<>();
    private static int commandLstUpIndex = 0;
    private static int commandLstDownIndex = 0;
    private static String currDataSet = "";
    private static List<ZOSConnection> connections = new ArrayList<>();
    private static ZOSConnection currConnection;
    private static List<String> currMembers = new ArrayList<>();
    private static TextTerminal<?> terminal;
    private static Commands commands;

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
            listUpCommands();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerm.registerHandler("DOWN", t -> {
            listDownCommands();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

    private static void listUpCommands() {
        if (commandLstUpIndex == 0)
            return;
        terminal.resetLine();
        terminal.printf("> " + commandLst.get(commandLstUpIndex - 1));
        commandLstUpIndex--;
    }

    private static void listDownCommands() {
        if (commandLstDownIndex == commandLst.size() - 1)
            return;
        terminal.resetLine();
        terminal.printf("> " + commandLst.get(commandLstDownIndex));
        commandLstDownIndex++;
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        terminal = textIO.getTextTerminal();
        commands = new Commands(connections, terminal);
        if (currConnection == null) {
            terminal.println(Constants.NO_CONNECTIONS);
        } else {
            terminal.println("Connected to " + currConnection.getHost() + " with user " + currConnection.getUser());
        }
        String[] command;
        String commandLine = "";
        while (!"end".equalsIgnoreCase(commandLine)) {
            commandLine = textIO.newStringInputReader().withMaxLength(80).read(">");
            command = commandLine.split(" ");
            if ("rm".equals(command[0])) {
                terminal.printf("Are you sure you want to delete y/n");
                commandLine = textIO.newStringInputReader().withMaxLength(80).read("?");
                if (!"y".equalsIgnoreCase(commandLine) && !"yes".equalsIgnoreCase(commandLine))
                    continue;
            }

            // if > was added by listCommands() then remove it..
            if (">".equals(command[0])) {
                String[] newCommand = copyCommand(command);
                executeCommand(newCommand);
            } else {
                executeCommand(command);
            }
        }

        textIO.dispose();
    }

    private static void executeCommand(String[] params) {
        final var command = params[0];
        String param;
        addToCommandLst(params);

        switch (command.toLowerCase()) {
            case "cancel":
                if (params.length == 1)
                    return;
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.cancel(currConnection, param);
                break;
            case "cat":
                if (params.length == 1)
                    return;
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.cat(currConnection, currDataSet, param);
                break;
            case "cd":
                if (params.length == 1)
                    return;
                if (isParamsExceeded(2, params))
                    return;
                currMembers = new ArrayList<>();
                currDataSet = commands.cd(currConnection, currDataSet, params[1].toUpperCase());
                if (currConnection != null)
                    dataSets.put(currConnection.getHost(), currDataSet);
                break;
            case "change":
                if (params.length == 1)
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
                if (params.length < 3)
                    return;
                if (isParamsExceeded(3, params))
                    return;
                commands.copy(currConnection, currDataSet, params);
                break;
            case "count":
                if (params.length == 1) {
                    terminal.printf(Constants.MISSING_COUNT_PARAM + "\n");
                    return;
                }
                if (!("members".equalsIgnoreCase(params[1]) || "datasets".equalsIgnoreCase(params[1]))) {
                    terminal.printf(Constants.MISSING_COUNT_PARAM + "\n");
                    return;
                }
                if (isParamsExceeded(2, params))
                    return;
                param = params[1];
                commands.count(currConnection, currDataSet, param);
                break;
            case "end":
                break;
            case "get":
                if (params.length == 1)
                    return;
                if (isParamsExceeded(2, params))
                    return;
                commands.get(currConnection, params);
                break;
            case "history":
                if (isParamsExceeded(1, params))
                    return;
                commands.history(commandLst);
                break;
            case "ls":
                if (isParamsExceeded(3, params))
                    return;
                if (params.length == 2 && Util.isDataSet(params[1])) {
                    commands.ls(currConnection, params[1]);
                    return;
                }
                if (params.length == 2 && !"-l".equalsIgnoreCase(params[1])) {
                    terminal.printf(Constants.INVALID_COMMAND + "\n");
                    return;
                }
                if (params.length == 3 && !"-l".equalsIgnoreCase(params[1])) {
                    terminal.printf(Constants.INVALID_COMMAND + "\n");
                    return;
                }
                if (params.length == 3 && "-l".equalsIgnoreCase(params[1])) {
                    commands.lsl(currConnection, params[2]);
                    return;
                }
                if (params.length == 2 && "-l".equalsIgnoreCase(params[1])) {
                    currMembers = commands.lsl(currConnection, currDataSet);
                    return;
                }
                if (currDataSet.isEmpty())
                    return;
                currMembers = commands.ls(currConnection, currDataSet);
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
                if (currDataSet.isEmpty())
                    return;
                terminal.printf(currDataSet + "\n");
                break;
            case "rm":
                if (isParamsExceeded(2, params))
                    return;
                if (params.length == 1)
                    return;
                param = params[1];
                commands.rm(currConnection, currDataSet, param);
                break;
            case "submit":
                if (params.length == 1)
                    return;
                if (isParamsExceeded(2, params))
                    return;
                if (currDataSet.isEmpty())
                    return;
                param = params[1];
                commands.submit(currConnection, currDataSet, param);
                break;
            case "tail":
                if (params.length == 1)
                    return;
                if (isParamsExceeded(3, params))
                    return;
                commands.tail(currConnection, params);
                break;
            case "uname":
                if (currConnection != null) {
                    terminal.printf(
                            "hostname: " + currConnection.getHost() + ", port: " + currConnection.getZosmfPort() + "\n");
                } else {
                    terminal.printf(Constants.NO_INFO + "\n");
                }
                break;
            case "v":
            case "visited":
                if (isParamsExceeded(1, params))
                    return;
                if (currDataSet.isEmpty())
                    return;
                for (String key : dataSets.keySet()) {
                    List<String> lst = dataSets.get(key);
                    lst.forEach(l -> terminal.printf(l.toUpperCase() + " ==> " + key + "\n"));
                }
                break;
            case "whoami":
                if (connections != null)
                    terminal.printf(currConnection.getUser() + "\n");
                break;
            default:
                terminal.printf(Constants.INVALID_COMMAND + "\n");
        }
    }

    private static void addToCommandLst(String[] params) {
        StringBuilder str = new StringBuilder();
        Arrays.stream(params).forEach(p -> str.append(p + " "));
        String command = str.toString();
        if (!command.startsWith("history")) {
            commandLst.add(str.toString());
            commandLstUpIndex = commandLst.size();
            commandLstDownIndex = 0;
        }
    }

    private String[] copyCommand(String[] command) {
        int newSize = command.length - 1;
        String newCommand[] = new String[newSize];
        for (int i = 1, j = 0; i < command.length; i++, j++) {
            newCommand[j] = command[i];
        }
        return newCommand;
    }

    private static boolean isParamsExceeded(int num, String[] commands) {
        if (commands.length > num) {
            terminal.printf(Constants.TOO_MANY_PARAMETERS + "\n");
            return true;
        }
        return false;
    }

}
