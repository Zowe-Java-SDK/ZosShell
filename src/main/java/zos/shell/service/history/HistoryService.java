package zos.shell.service.history;

import com.google.common.base.Strings;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.singleton.TerminalSingleton;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryService.class);

    private final TextTerminal<?> terminal;
    private final List<String> commandLst = new LinkedList<>();
    private final CircularLinkedList<String> circularLinkedList = new CircularLinkedList<>();

    public HistoryService(final TextTerminal<?> terminal) {
        LOG.debug("*** HistoryService ***");
        this.terminal = terminal;
    }

    public void listUpCommands() {
        LOG.debug("*** listUpCommands ***");
        if (circularLinkedList.getSize() > 1) {
            TerminalSingleton.getInstance().getMainTerminal()
                    .replaceInput(circularLinkedList.back().trim(), false);
        } else if (circularLinkedList.getSize() == 1) {
            TerminalSingleton.getInstance().getMainTerminal()
                    .replaceInput(circularLinkedList.head.getData().trim(), false);
        }
    }

    public void listDownCommands() {
        LOG.debug("*** listDownCommands ***");
        if (circularLinkedList.getSize() > 1) {
            TerminalSingleton.getInstance().getMainTerminal()
                    .replaceInput(circularLinkedList.forward().trim(), false);
        } else if (circularLinkedList.getSize() == 1) {
            TerminalSingleton.getInstance().getMainTerminal()
                    .replaceInput(circularLinkedList.head.getData().trim(), false);
        }
    }

    public void addHistory(final String[] params) {
        LOG.debug("*** addHistory ***");
        var str = new StringBuilder();
        Arrays.stream(params).forEach(p -> {
            str.append(p);
            str.append(" ");
        });
        var command = str.toString();
        if (!command.startsWith("history")) {
            if (commandLst.size() == Constants.HISTORY_SIZE) {
                commandLst.remove(0);
            }
            if (commandLst.isEmpty() || !getLastHistory().equals(command)) {
                commandLst.add(command);
                circularLinkedList.add(command);
            }
        }
        // reset the currNode pointer used to handle history scrolling...
        circularLinkedList.currNode = null;
    }

    public void displayHistory() {
        LOG.debug("*** displayHistory 1 ***");
        displayAll();
    }

    public void displayHistory(final String param) {
        LOG.debug("*** displayHistory 2 ***");
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
        int size = commandLst.size();
        if (num > size) {
            displayAll();
        } else {
            var startIndex = size - num;
            for (var i = startIndex; i < size; i++) {
                display(i);
            }
        }
    }

    public String getHistoryByIndex(final int index) {
        LOG.debug("*** getHistoryByIndex ***");
        if (index > commandLst.size() - 1 || commandLst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return null;
        }
        return commandLst.get(index);
    }

    public String getLastHistory() {
        LOG.debug("*** getLastHistory ***");
        if (commandLst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return null;
        }
        return commandLst.get(commandLst.size() - 1);
    }

    public String getLastHistoryByValue(final String str) {
        LOG.debug("*** getLastHistoryByValue ***");
        List<String> lst = commandLst.stream().filter(c -> c.startsWith(str.toLowerCase())).collect(Collectors.toList());
        if (lst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return null;
        }
        return lst.get(lst.size() - 1);
    }

    private void displayAll() {
        LOG.debug("*** displayAll ***");
        IntStream.range(0, commandLst.size()).forEach(this::display);
    }

    private void display(final int index) {
        LOG.debug("*** display ***");
        var orderNum = Strings.padStart(String.valueOf(index + 1), 4, ' ');
        var historyRow = orderNum + Constants.ARROW + commandLst.get(index);
        terminal.println(historyRow);
    }

}
