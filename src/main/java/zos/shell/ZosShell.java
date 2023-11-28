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
import zos.shell.commands.Commands;
import zos.shell.commands.History;
import zos.shell.config.Config;
import zos.shell.config.Credentials;
import zos.shell.data.Environment;
import zos.shell.data.SearchDictionary;
import zos.shell.dto.Output;
import zos.shell.utility.Util;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

import javax.swing.*;
import java.util.*;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final Logger LOG = LoggerFactory.getLogger(ZosShell.class);

    private static final ListMultimap<String, String> dataSets = ArrayListMultimap.create();
    private static String currDataSet = "";
    private static int currDataSetMax = 0;
    private static final List<ZosConnection> connections = new ArrayList<>();
    private static final Map<String, SshConnection> SshConnections = new HashMap<>();
    private static ZosConnection currConnection;
    private static TextTerminal<?> terminal;
    private static Commands commands;
    private static History history;
    private static Output commandOutput;
    private static final SwingTextTerminal mainTerminal = new SwingTextTerminal();
    private static final int defaultFontSize = 10;
    private static int fontSize = defaultFontSize;
    private static boolean fontSizeChanged = false;
    private static boolean disableKeys = false;
    private static TextIO mainTextIO;

    public static void main(String[] args) {
        LOG.debug("*** main ***");
        Credentials.readCredentials(connections, SshConnections);
        if (!connections.isEmpty()) {
            currConnection = connections.get(0);
        }
        mainTerminal.init();
        setTerminalProperties();
        mainTextIO = new TextIO(mainTerminal);
        new ZosShell().accept(mainTextIO, null);
    }

    private static void setTerminalProperties() {
        LOG.debug("*** setTerminalProperties ***");
        var title = "";
        if (currConnection != null) {
            title = " - " + currConnection.getHost().toUpperCase();
        }
        mainTerminal.setPaneTitle(Constants.APP_TITLE + title);

        final var iconURL = ZosShell.class.getResource("/image/zowe-icon.png");
        if (iconURL != null) {
            final var icon = new ImageIcon(iconURL);
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
            history.listUpCommands(Util.getPrompt());
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("DOWN", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            history.listDownCommands(Util.getPrompt());
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
            System.out.println(mainTerminal.getTextPane().getText());
            mainTerminal.print(Util.getPrompt() + " Increased font size to " + fontSize + ".");
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
                mainTerminal.print(Util.getPrompt() + " Decreased font size to " + fontSize + ".");
                fontSizeChanged = true;
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("TAB", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            final var items = mainTerminal.getTextPane().getText().split(Util.getPrompt());
            var candidateStr = items[items.length - 1].trim();
            candidateStr = candidateStr.replaceAll("[\\p{Cf}]", "");
            if (candidateStr.contains(" ")) {  // invalid look up
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            final var candidateLst = SearchDictionary.search(candidateStr);
            if (!candidateLst.isEmpty()) {
                mainTerminal.moveToLineStart();
                if (candidateLst.size() == 1) {
                    mainTerminal.print(Util.getPrompt() + " " + candidateLst.get(0));
                } else {
                    mainTextIO.newStringInputReader().withDefaultValue("hit enter to skip")
                            .read((Util.getPrompt() + " " + candidateLst));
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
        var commandLine = "";
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
                    final var dataSetMember = Util.getDatasetAndMember(command[1]);
                    if (!currDataSet.isBlank() && dataSetMember != null) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (!currDataSet.isBlank() && Util.isMember(command[1])) {
                        final var candidate = currDataSet + "(" + command[1] + ")";
                        terminal.printf("Are you sure you want to delete " + candidate + " y/n");
                    } else if (currDataSet.isBlank() && dataSetMember != null) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (currDataSet.isBlank() && Util.isDataSet(command[1])) {
                        terminal.printf("Are you sure you want to delete " + command[1] + " y/n");
                    } else if (!currDataSet.isBlank() && ("*".equals(command[1]) || ".".equals(command[1]))) {
                        terminal.printf("Are you sure you want to delete all from " + currDataSet + " y/n");
                    } else if (!currDataSet.isBlank() && !Util.isDataSet(command[1]) && !Util.isMember(command[1])) {
                        terminal.println("No valid dataset, member or dataset(member) value provided, try again...");
                        break;
                    } else if (currDataSet.isBlank() && !Util.isDataSet(command[1]) && !Util.isMember(command[1])) {
                        terminal.println("No valid dataset or dataset(member) value provided, try again...");
                        break;
                    } else if (currDataSet.isBlank()) {
                        terminal.println(Constants.DATASET_NOT_SPECIFIED);
                        break;
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
            executeCommand(history.filterCommand(Util.getPrompt(), command));
        }

        textIO.dispose();
    }

    private static String[] removePrompt(String[] command) {
        LOG.debug("*** removePrompt ***");
        if (Constants.DEFAULT_PROMPT.equals(command[0])) {
            final var size = command.length;
            final var newCmdArr = new String[size - 1];
            final var newCmdLst = new ArrayList<>(Arrays.asList(command).subList(1, size));
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
            final var str = new StringBuilder();
            for (var i = 0; i < command.length; i++) {
                str.append(command[i]);
                if (i + 1 != command.length) {
                    str.append(" ");
                }
            }

            final var cmd = str.toString();
            if (cmd.length() == 1) {
                terminal.println(Constants.MISSING_PARAMETERS);
                return null;
            }

            final var subStr = cmd.substring(1);
            final var isStrNum = Util.isStrNum(subStr);

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
        final var command = params[0];
        String param;
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
                commandOutput = commands.cat(currConnection, currDataSet, param);
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
                if (!currDataSet.isBlank()) {
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
            case "cps":
            case "copys":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsMissing(2, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                commands.copySequential(currConnection, currDataSet, params);
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
            case "d":
            case "download":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(3, params)) {
                    return;
                }
                if (isCurrDataSetNotSpecified()) {
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
                commands.downloadJob(currConnection, param, isAll);
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
                commands.files(currDataSet);
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
                commands.grep(currConnection, params[1], params[2], currDataSet);
                break;
            case "h":
            case "help":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                if (params.length == 1) {
                    commandOutput = commands.help();
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
                if (params.length == 3 && ("-l".equals(params[1]) || "--l".equals(params[1]))) {
                    final var isAttributes = !"--l".equals(params[1]);
                    final var value = params[2];
                    final var size = params[2].length();
                    if (size <= 9 && value.charAt(size - 1) == '*') {  // is member with wild card specified...
                        if (isCurrDataSetNotSpecified()) {
                            return;
                        }
                        final var index = value.indexOf("*");
                        final var member = value.substring(0, index);
                        if (Util.isMember(member)) {  // validate member value without wild card char...
                            commands.lsl(currConnection, value, currDataSet, isAttributes);
                        } else {
                            terminal.println(Constants.INVALID_MEMBER);
                        }
                        return;
                    } else if (Util.isMember(value)) {  // is member without wild card specified...
                        if (isCurrDataSetNotSpecified()) {
                            return;
                        }
                        commands.lsl(currConnection, value, currDataSet, isAttributes);
                        return;
                    } else if (Util.isDataSet(value)) {  // is dataset specified at this point...
                        commands.lsl(currConnection, null, value, isAttributes);
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
                    final var isAttributes = !"--l".equals(params[1]);
                    commands.lsl(currConnection, currDataSet, isAttributes);
                    addVisited();
                    return;
                }
                if (params.length == 2 && Util.isDataSet(params[1])) {
                    commands.ls(currConnection, params[1]);
                    return;
                }
                if (isCurrDataSetNotSpecified()) {
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
            case "mkdir":
                if (isParamsMissing(1, params)) {
                    return;
                }
                disableKeys = true;
                try {
                    commands.mkdir(currConnection, mainTextIO, currDataSet, params[1]);
                } catch (Exception ignore) {
                }
                disableKeys = false;
                break;
            case "mvs":
                if (isParamsMissing(1, params)) {
                    return;
                }
                final var mvsCommandCandidate = getCommandFromParams(params);
                final var mvsCommandCount = mvsCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(mvsCommandCount, mvsCommandCandidate)) {
                    commandOutput = commands.mvsCommand(currConnection, mvsCommandCandidate.toString());
                }
                break;
            case "ps":
                if (isParamsExceeded(2, params)) {
                    return;
                }
                if (params.length > 1) {
                    commandOutput = commands.ps(currConnection, params[1]);
                } else {
                    commandOutput = commands.ps(currConnection);
                }
                break;
            case "pj":
            case "purgejob":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(2, params)) {
                    return;
                }
                commands.purgeJob(currConnection, params[1].toUpperCase());
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
                // command parameter check is done before this call
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
            case "tj":
            case "tailjob":
                if (isParamsMissing(1, params)) {
                    return;
                }
                if (isParamsExceeded(4, params)) {
                    return;
                }
                commandOutput = commands.tailJob(currConnection, params);
                break;
            case "t":
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
            case "tso":
                if (isParamsMissing(1, params)) {
                    return;
                }
                final var acctNum = Environment.getInstance().getValueByKeyName("ACCTNUM");
                final var tsoCommandCandidate = getCommandFromParams(params);
                final var tsoCommandCount = tsoCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(tsoCommandCount, tsoCommandCandidate)) {
                    commandOutput = commands.tsoCommand(currConnection, acctNum, tsoCommandCandidate.toString());
                }
                break;
            case "uname":
                if (isParamsExceeded(1, params)) {
                    return;
                }
                commands.uname(currConnection);
                break;
            case "ussh":
                if (isParamsMissing(1, params)) {
                    return;
                }
                final var ussCommandCandidate = getCommandFromParams(params);
                final var ussCommandCount = ussCommandCandidate.codePoints().filter(ch -> ch == '\"').count();
                if (isCommandValid(ussCommandCount, ussCommandCandidate)) {
                    commands.ussh(terminal, currConnection, SshConnections, ussCommandCandidate.toString());
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
                    final var lst = dataSets.get(key);
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
        final var command = new StringBuilder();
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
