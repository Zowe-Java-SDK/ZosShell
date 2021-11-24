package com.history;

import org.beryx.textio.TextTerminal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class History {

    private final TextTerminal<?> terminal;
    private List<String> commandLst = new LinkedList<>();
    private static int commandLstUpIndex = 0;
    private static int commandLstDownIndex = 0;

    public History(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void listUpCommands() {
        if (commandLstUpIndex == 0) {
            commandLstUpIndex = commandLst.size();
            return;
        }
        terminal.resetLine();
        terminal.printf("> " + commandLst.get(commandLstUpIndex - 1));
        commandLstUpIndex--;
    }

    public void listDownCommands() {
        if (commandLstDownIndex == commandLst.size()) {
            commandLstDownIndex = 0;
            return;
        }
        terminal.resetLine();
        terminal.printf("> " + commandLst.get(commandLstDownIndex));
        commandLstDownIndex++;
    }

    public void addHistory(String[] params) {
        StringBuilder str = new StringBuilder();
        Arrays.stream(params).forEach(p -> str.append(p + " "));
        String command = str.toString();
        if (!command.startsWith("history")) {
            commandLst.add(str.toString());
            commandLstUpIndex = commandLst.size();
            commandLstDownIndex = 0;
        }
    }

    public static String[] filterCommand(String[] command) {
        if (!">".equals(command[0]))
            return command;

        // remove ">" first parameter, added by listUpCommands or listDownCommands method
        int newSize = command.length - 1;
        String newCommand[] = new String[newSize];
        for (int i = 1, j = 0; i < command.length; i++, j++) {
            newCommand[j] = command[i];
        }
        return newCommand;
    }

    public List<String> getCommandLst() {
        return commandLst;
    }

}
