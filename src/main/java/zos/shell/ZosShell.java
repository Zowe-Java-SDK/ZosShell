package zos.shell;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandRouter;
import zos.shell.constants.Constants;
import zos.shell.service.history.HistoryService;
import zos.shell.singleton.ConnSingleton;
import zos.shell.singleton.HistorySingleton;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.singleton.configuration.ConfigSingleton;
import zos.shell.utility.PromptUtil;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.utility.ValidateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final Logger LOG = LoggerFactory.getLogger(ZosShell.class);

    public static void main(String[] args) {
        LOG.debug("*** main ***");
        // initialize all Singleton objects needed for Shell processing and tracking
        // initialize TerminalSingleton object
        var terminalSingleton = TerminalSingleton.getInstance();
        terminalSingleton.setMainTerminal(new SwingTextTerminal());
        terminalSingleton.getMainTerminal().init();
        terminalSingleton.setMainTextIO(new TextIO(terminalSingleton.getMainTerminal()));

        // initialize ConfigSingleton object
        try {
            ConfigSingleton.getInstance().readConfig();
        } catch (NumberFormatException | IOException e) {
            var mainTerminal = terminalSingleton.getMainTerminal();
            mainTerminal.println("Error reading or parsing console.json file, try again...");
            mainTerminal.println("Error: " + e.getMessage());
            mainTerminal.read(true);
            mainTerminal.dispose();
            throw new RuntimeException(e);
        }

        // Initialize TerminalSingleton terminal properties
        terminalSingleton.setTerminalProperties();

        // initialize ConnSingleton object
        var connSingleton = ConnSingleton.getInstance();
        connSingleton.setCurrZosConnection(ConfigSingleton.getInstance().getZosConnectionByIndex(0));
        connSingleton.setCurrSshConnection(ConfigSingleton.getInstance().getSshConnectionByIndex(0));
        var title = Constants.APP_TITLE + " - " + connSingleton.getCurrSshConnection().getHost().toUpperCase();
        TerminalSingleton.getInstance().getMainTerminal().setPaneTitle(title);

        // set TerminalSingleton terminal window font size
        if (ConfigSingleton.getInstance().getConfigSettings() != null &&
                ConfigSingleton.getInstance().getConfigSettings().getWindow() != null &&
                ConfigSingleton.getInstance().getConfigSettings().getWindow().getFontsize() != null) {
            var fontSize = Integer.parseInt(ConfigSingleton.getInstance().getConfigSettings().getWindow().getFontsize());
            terminalSingleton.setFontSize(fontSize);
        }

        new ZosShell().accept(TerminalSingleton.getInstance().getMainTextIO(), null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        LOG.debug("*** accept ***");

        // continue to initialize TerminalSingleton object
        TerminalSingleton.getInstance().setTerminal(textIO.getTextTerminal());
        TerminalSingleton.getInstance().getTerminal().setBookmark("top");

        // with TerminalSingleton fully initialize update window terminal with the configured properties
        ConfigSingleton.getInstance().updateWindowSittings(TerminalSingleton.getInstance().getTerminal());

        // initialize HistorySingleton object
        HistorySingleton.getInstance().setHistory(TerminalSingleton.getInstance().getTerminal());

        // local variables copies from singletons
        TextTerminal<?> terminal = TerminalSingleton.getInstance().getTerminal();
        var currConnection = ConnSingleton.getInstance().getCurrZosConnection();

        // display information on initial first connection defined and set for usage
        try {
            ValidateUtils.checkConnection(currConnection);
            var host = currConnection.getHost();
            var user = currConnection.getUser();
            terminal.println("Connected to " + host + " with user " + user + ".");
        } catch (Exception e) {
            var mainTerminal = TerminalSingleton.getInstance().getMainTerminal();
            mainTerminal.println("ERROR: Default connection invalid, try again...");
            mainTerminal.println("Error: " + e.getMessage());
            mainTerminal.read(true);
            mainTerminal.dispose();
            throw new RuntimeException(e);
        }

        var commandRouter = new CommandRouter(terminal);
        do {
            String input = textIO.newStringInputReader().withMaxLength(80).read(PromptUtil.getPrompt());
            if ("end".equalsIgnoreCase(input)) {
                break;
            }
            if (isFontSizeChanged()) {
                terminal.println("Front size set.");
                continue;
            }
            String[] command = input.split(" ");
            command = StrUtil.stripEmptyStrings(command);
            if (isPromptInput(command)) {
                continue;
            }
            command = removePrompt(command);
            if (isExclamationMark(command)) {
                command = retrieveFromHistory(command);
            }
            commandRouter.routeCommand(command);
        } while (true);

        textIO.dispose();
    }

    private boolean isExclamationMark(final String[] command) {
        LOG.debug("*** isExclamationMark ***");
        return command[0].startsWith("!");
    }

    private boolean isPromptInput(final String[] command) {
        LOG.debug("*** isPromptInput ***");
        // handle edge case where end user enters prompt as the only input, skip it and continue
        return command.length == 1 && Constants.DEFAULT_PROMPT.equals(command[0]);
    }

    private boolean isFontSizeChanged() {
        LOG.debug("*** isFontSizeChanged ***");
        if (TerminalSingleton.getInstance().isFontSizeChanged()) {
            TerminalSingleton.getInstance().setFontSizeChanged(false);
            return true;
        }
        return false;
    }

    private static String[] removePrompt(String[] command) {
        LOG.debug("*** removePrompt ***");
        if (Constants.DEFAULT_PROMPT.equals(command[0])) {
            int size = command.length;
            String[] newCmdArr = new String[size - 1];
            List<String> newCmdLst = new ArrayList<>(Arrays.asList(command).subList(1, size));
            for (var i = 0; i < newCmdLst.size(); i++) {
                newCmdArr[i] = newCmdLst.get(i);
            }
            command = newCmdArr;
        }
        return command;
    }

    private String[] retrieveFromHistory(String[] command) {
        LOG.debug("*** retrieveFromHistory ***");
        // local variables copies from singletons
        TextTerminal<?> terminal = TerminalSingleton.getInstance().getTerminal();
        HistoryService historyService = HistorySingleton.getInstance().getHistory();

        var str = new StringBuilder();
        for (var i = 0; i < command.length; i++) {
            str.append(command[i]);
            if (i + 1 != command.length) {
                str.append(" ");
            }
        }

        var cmd = str.toString();
        if (cmd.length() == 1) {
            terminal.println(Constants.MISSING_PARAMETERS);
            return null;
        }

        var subStr = cmd.substring(1);
        boolean isStrNum = StrUtil.isStrNum(subStr);

        String newCmd;
        if ("!".equals(subStr)) {
            newCmd = historyService.getLastHistory();
        } else if (isStrNum) {
            newCmd = historyService.getHistoryByIndex(Integer.parseInt(subStr) - 1);
        } else {
            newCmd = historyService.getLastHistoryByValue(subStr);
        }

        // set new command from history content
        command = newCmd != null ? newCmd.split(" ") : null;
        return command;
    }

}
