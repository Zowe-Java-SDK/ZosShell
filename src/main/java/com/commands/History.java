package com.commands;

import com.Constants;
import org.beryx.textio.TextTerminal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class History {

    private final TextTerminal<?> terminal;
    private final List<String> commandLst = new LinkedList<>();
    private static int commandLstUpIndex = 0;
    private static int commandLstDownIndex = 0;

    public History(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void listUpCommands(String prompt) {
        if (commandLstUpIndex == 0) {
            commandLstUpIndex = commandLst.size();
            return;
        }
        terminal.resetLine();
        terminal.printf(prompt + " " + commandLst.get(commandLstUpIndex - 1));
        commandLstUpIndex--;
    }

    public void listDownCommands(String prompt) {
        if (commandLstDownIndex == commandLst.size()) {
            commandLstDownIndex = 0;
            return;
        }
        terminal.resetLine();
        terminal.printf(prompt + " " + commandLst.get(commandLstDownIndex));
        commandLstDownIndex++;
    }

    public void addHistory(String[] params) {
        StringBuilder str = new StringBuilder();
        Arrays.stream(params).forEach(p -> {
            str.append(p);
            str.append(" ");
        });
        String command = str.toString();
        if (!command.startsWith("history")) {
            commandLst.add(str.toString());
            commandLstUpIndex = commandLst.size();
            commandLstDownIndex = 0;
        }
    }

    public void displayHistory() {
        commandLst.forEach(terminal::println);
    }

    public void displayHistory(String param) {
        int num;
        try {
            num = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            terminal.println(Constants.INVALID_NUMBER);
            return;
        }

        if (this.commandLst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return;
        }
        var size = commandLst.size();
        if (num > size)
            commandLst.forEach(terminal::println);
        else {
            var startIndex = size - num;
            for (int i = startIndex; i < size; i++)
                terminal.println(commandLst.get(i));
        }
    }

    public String[] filterCommand(String prompt, String[] command) {
        if (!prompt.equals(command[0]))
            return command;

        // remove ">" first parameter, added by listUpCommands or listDownCommands method
        var newSize = command.length - 1;
        String[] newCommand = new String[newSize];
        for (int i = 1, j = 0; i < command.length; i++, j++) {
            newCommand[j] = command[i];
        }
        return newCommand;
    }

    public String getHistoryByIndex(int index) {
        if (index > commandLst.size() - 1 || commandLst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return null;
        }
        return commandLst.get(index);
    }

    public String getLastHistoryByValue(String str) {
        var lst = commandLst.stream().filter(c -> c.startsWith(str.toLowerCase())).collect(Collectors.toList());
        if (lst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return null;
        }
        return lst.get(lst.size() - 1);
    }

}
