package com;

import com.command.Commands;
import com.credential.Credentials;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static LinkedHashSet<String> dataSets = new LinkedHashSet<>();
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
        String[] commands;
        String commandLine = "";
        while (!"end".equalsIgnoreCase(commandLine)) {
            commandLine = textIO.newStringInputReader().withMaxLength(80).read(">");
            System.out.print("> ");
            commands = commandLine.split(" ");
            executeCommand(commands);
        }

        textIO.dispose();
    }

    private static void executeCommand(String[] params) {
        String command = params[0];
        String param;

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
                currDataSet = commands.cd(currDataSet, params[1]);
                dataSets.add(currDataSet);
                break;
            case "change":
                if (params.length == 1)
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
                commands.copy(currConnection, currMembers, currDataSet, params);
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
            case "ls":
                if (isParamsExceeded(2, params))
                    return;
                if (params.length == 2 && Util.isDataSet(params[1])) {
                    commands.ls(currConnection, params[1]);
                    return;
                }
                if (params.length == 2 && !"-l".equalsIgnoreCase(params[1])) {
                    terminal.printf(Constants.TOO_MANY_PARAMETERS + "\n");
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
            case "uname":
                if (currConnection != null) {
                    terminal.printf(
                            "hostname: " + currConnection.getHost() + ", port: " + currConnection.getZosmfPort() + "\n");
                } else {
                    terminal.printf(Constants.NO_INFO + "\n");
                }
                break;
            case "visited":
                if (isParamsExceeded(1, params))
                    return;
                if (currDataSet.isEmpty())
                    return;
                dataSets.forEach(terminal::println);
                break;
            case "whoami":
                if (isParamsExceeded(1, params))
                    return;
                terminal.printf(currConnection.getUser() + "\n");
                break;
            default:
                terminal.printf(Constants.INVALID_COMMAND + "\n");
        }
    }

    private static boolean isParamsExceeded(int num, String[] commands) {
        if (commands.length > num) {
            terminal.printf(Constants.TOO_MANY_PARAMETERS + "\n");
            return true;
        }
        return false;
    }

}
