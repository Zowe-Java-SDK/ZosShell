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
import zos.shell.commands.Commands;
import zos.shell.commands.History;
import zos.shell.config.Config;
import zos.shell.config.Credentials;
import zos.shell.dto.Output;
import zos.shell.utility.Util;
import zowe.client.sdk.core.SSHConnection;
import zowe.client.sdk.core.ZOSConnection;

import java.util.*;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final ListMultimap<String, String> dataSets = ArrayListMultimap.create();
    private static String currDataSet = "";
    private static int currDataSetMax = 0;
    private static final List<ZOSConnection> connections = new ArrayList<>();
    private static final Map<String, SSHConnection> sshConnections = new HashMap<>();
    private static ZOSConnection currConnection;
    private static TextTerminal<?> terminal;
    private static Commands commands;
    private static History history;
    private static Output commandOutput;
    private static final SwingTextTerminal mainTerminal = new SwingTextTerminal();
    private static final int defaultFontSize = 10;
    private static int fontSize = defaultFontSize;
    private static boolean fontSizeChanged = false;

    public static void main(String[] args) {
        Credentials.readCredentials(connections, sshConnections);
        if (!connections.isEmpty()) {
            currConnection = connections.get(0);
        }
        mainTerminal.init();
        setTerminalProperties();
        var mainTextIO = new TextIO(mainTerminal);
        new ZosShell().accept(mainTextIO, null);
    }

    private static void setTerminalProperties() {
        var title = "";
        if (currConnection != null) {
            title = " - " + currConnection.getHost().toUpperCase();
        }
        mainTerminal.setPaneTitle(Constants.APP_TITLE + title);
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
        mainTerminal.registerHandler("ctrl UP", t -> {
            fontSize++;
            mainTerminal.setInputFontSize(fontSize);
            mainTerminal.setPromptFontSize(fontSize);
            mainTerminal.moveToLineStart();
            mainTerminal.print("> Increased font size to " + fontSize + ".");
            fontSizeChanged = true;
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("ctrl DOWN", t -> {
            if (fontSize != defaultFontSize) {
                fontSize--;
                mainTerminal.setInputFontSize(fontSize);
                mainTerminal.setPromptFontSize(fontSize);
                mainTerminal.moveToLineStart();
                mainTerminal.print("> Decreased font size to " + fontSize + ".");
                fontSizeChanged = true;
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        terminal = textIO.getTextTerminal();
        terminal.setBookmark("top");
        final var config = new Config(terminal);
        if (config.getFrontSize() != null) {
            try {
                fontSize = Integer.parseInt(config.getFrontSize());
            } catch (NumberFormatException ignore) {
                // might want to log bad frontSize value from configuration
            }
        }
        commands = new Commands(connections, terminal);
        history = new History(terminal);
        if (currConnection == null) {
            terminal.println(Constants.NO_CONNECTIONS);
        } else {
            terminal.println("Connected to " + currConnection.getHost() + " with user " + currConnection.getUser() + ".");
        }

        String[] command;
        String commandLine = "";
        while (!"end".equalsIgnoreCase(commandLine)) {
            commandLine = textIO.newStringInputReader().withMaxLength(80).read(Util.getPrompt());
            if (fontSizeChanged) {
                terminal.println("Front size set.");
                fontSizeChanged = false;
                continue;
            }
            command = commandLine.split(" ");
            if (Arrays.stream(command).anyMatch(String::isEmpty)) {  // handle multiple empty spaces specified
                command = Util.stripEmptyStrings(command);
            }

            command = exclamationMark(command);
            if (command == null) {
                continue;
            }

            if ("rm".equals(command[0])) {
                if (!currDataSet.isEmpty()) {
                    terminal.printf("Are you sure you want to delete from " + currDataSet + " y/n");
                } else {
                    terminal.printf("Are you sure you want to delete y/n");
                }
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
        if ((command[0].equals(">") && command.length >= 2 && command[1].startsWith("!")) || command[0].startsWith("!")) {
            final var str = new StringBuilder();
            if (">".equals(command[0])) {
                for (int i = 1; i < command.length; i++) {
                    str.append(command[i]);
                    if (i + 1 != command.length) {
                        str.append(" ");
                    }
                }
            } else {
                for (int i = 0; i < command.length; i++) {
                    str.append(command[i]);
                    if (i + 1 != command.length) {
                        str.append(" ");
                    }
                }
            }

            final var cmd = str.toString();
            if (cmd.length() == 1) {
                terminal.println(Constants.MISSING_PARAMETERS);
                return null;
            }

            final var subStr = cmd.substring(1);
            var isStrNum = Util.isStrNum(subStr);

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
                commandOutput = commands.browse(currConnection, params);
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
                commandOutput = new Output(param, commands.cat(currConnection, currDataSet, param));
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
                if (commandOutput != null) {
                    commandOutput.getOutput().setLength(0);
                    commandOutput = null;
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
                if (params.length == 3 && !"-l".equalsIgnoreCase(params[1])) {
                    terminal.println(Constants.INVALID_COMMAND);
                    return;
                }
                if (params.length == 3 && "-l".equalsIgnoreCase(params[1])) {
                    final var value = params[2];
                    final var size = params[2].length();
                    if (size <= 9 && value.charAt(size - 1) == '*') {  // is member with wild card specified...
                        final var index = value.indexOf("*");
                        final var member = value.substring(0, index);
                        if (Util.isMember(member)) {  // validate member value without wild card char...
                            commands.lsl(currConnection, value, currDataSet);
                            return;
                        } else {
                            terminal.println(Constants.INVALID_MEMBER);
                        }
                    } else if (Util.isMember(value)) {  // is member without wild card specified...
                        commands.lsl(currConnection, value, currDataSet);
                        return;
                    } else if (Util.isDataSet(value)) {  // is dataset specified at this point...
                        commands.lsl(currConnection, null, value);
                        return;
                    } else {  // must be an invalid member or dataset specified...
                        terminal.println(Constants.INVALID_DATASET_AND_MEMBER);
                    }
                }
                if (params.length == 2 && "-l".equalsIgnoreCase(params[1])) {
                    if (isCurrDataSetNotSpecified()) {
                        return;
                    }
                    commands.lsl(currConnection, currDataSet);
                    addVisited();
                    return;
                }
                if (params.length == 2 && Util.isDataSet(params[1])) {
                    commands.ls(currConnection, params[1]);
                    return;
                }
                if (params.length == 2 && (params[1].length() <= 9 && params[1].charAt(params[1].length() - 1) == '*')) {
                    final var value = params[1];
                    final var index = value.indexOf("*");
                    final var member = value.substring(0, index);
                    if (Util.isMember(member)) {
                        commands.ls(currConnection, value, currDataSet);
                        return;
                    }
                }
                if (params.length == 2 && Util.isMember(params[1])) {
                    commands.ls(currConnection, params[1], currDataSet);
                    return;
                }
                if (params.length == 2) {
                    terminal.println(Constants.INVALID_DATASET_AND_MEMBER);
                    return;
                }
                if (params.length == 1) {
                    commands.ls(currConnection, currDataSet);
                    return;
                }
                break;
            case "mvs":
                if (isParamsMissing(1, params)) {
                    return;
                }
                final var mvsCommandCandidate = getCommandFromParams(params);
                final var mvsCommandCount = mvsCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(mvsCommandCount, mvsCommandCandidate)) {
                    commands.mvsCommand(currConnection, mvsCommandCandidate.toString());
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
            case "purgejob":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                commands.purgeJob(currConnection, params[1]);
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
                commands.search(commandOutput, params[1]);
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
                commandOutput = commands.tailJob(currConnection, params);
                break;
            case "timeout":
                if (isParamsExceeded(2, params)) {
                    return;
                }
                if (params.length == 1) {
                    commands.timeOutValue();
                } else {
                    try {
                        commands.timeOutValue(Long.parseLong(params[1]));
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
            case "ussh":
                if (isParamsMissing(1, params)) {
                    return;
                }
                final var ussCommandCandidate = getCommandFromParams(params);
                final var ussCommandCount = ussCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(ussCommandCount, ussCommandCandidate)) {
                    commands.ussh(terminal, currConnection, sshConnections, ussCommandCandidate.toString());
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

    private static boolean isCommandValid(long count, StringBuilder commandCandidate) {
        if (count == 2 && commandCandidate.charAt(commandCandidate.length() - 1) == '\"') {
            return true;
        } else if (count == 2) {
            terminal.println(Constants.MVS_EXTRA_TEXT_INVALID_COMMAND);
        } else {
            terminal.println(Constants.MVS_INVALID_COMMAND);
        }
        return false;
    }

    private static StringBuilder getCommandFromParams(String[] params) {
        final var command = new StringBuilder();
        for (int i = 1; i < params.length; i++) {
            command.append(params[i]);
            if (i != params.length - 1) {
                command.append(" ");
            }
        }
        return command;
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
