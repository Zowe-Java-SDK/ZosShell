package zos.shell;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.commandcli.CommandRouter;
import zos.shell.constants.Constants;
import zos.shell.resolver.HistoryCommandResolver;
import zos.shell.singleton.ConnSingleton;
import zos.shell.singleton.HistorySingleton;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.singleton.configuration.ConfigSingleton;
import zos.shell.utility.PromptUtil;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnectionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final Logger LOG = LoggerFactory.getLogger(ZosShell.class);

    public static void main(String[] args) {
        LOG.debug("*** main ***");
        var connectionIdentifier = "";
        if (args.length > 0) {
            connectionIdentifier = args[0];
        }
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
            mainTerminal.println("Error reading or parsing config.json file, try again...");
            mainTerminal.println("Error: " + e.getMessage());
            mainTerminal.read(true);
            mainTerminal.dispose();
            throw new RuntimeException(e);
        }

        // initialize TerminalSingleton terminal properties
        terminalSingleton.setTerminalProperties();

        // initialize ConnSingleton object
        var connSingleton = ConnSingleton.getInstance();
        var numOfConnections = ConfigSingleton.getInstance().getZosConnections().size();

        // set ConnSingleton connection based on connection identifier if applicable
        if (!connectionIdentifier.isBlank()) {
            try {
                var numOfConnection = Integer.parseInt(connectionIdentifier) - 1;
                if (numOfConnection > 0 && numOfConnection <= numOfConnections) {
                    var zosConnection = ConfigSingleton.getInstance().getZosConnectionByIndex(numOfConnection);
                    connSingleton.setCurrZosConnection(zosConnection, numOfConnection);
                    var sshConnection = ConfigSingleton.getInstance().getSshConnectionByIndex(numOfConnection);
                    connSingleton.setCurrSshConnection(sshConnection);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // set ConnSingleton connection based on the first profile in the configuration file if applicable
        if (connSingleton.getCurrZosConnection() == null) {
            var index = 0;
            var zosConnection = ConfigSingleton.getInstance().getZosConnectionByIndex(index);
            connSingleton.setCurrZosConnection(zosConnection, index);
            var sshConnection = ConfigSingleton.getInstance().getSshConnectionByIndex(index);
            connSingleton.setCurrSshConnection(sshConnection);
        }
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
        ConfigSingleton.getInstance().updateWindowSettings(TerminalSingleton.getInstance().getTerminal());

        // initialize HistorySingleton object
        HistorySingleton.getInstance().setHistory(TerminalSingleton.getInstance().getTerminal());

        // local variables copies from singletons
        TextTerminal<?> terminal = TerminalSingleton.getInstance().getTerminal();
        var currConnection = ConnSingleton.getInstance().getCurrZosConnection();
        var currSshConnection = ConnSingleton.getInstance().getCurrSshConnection();

        // initialize the first connection definition and prompt for username and password if applicable.
        try {
            var host = currConnection.getHost();
            var zosmfport = currConnection.getZosmfPort();
            var username = currConnection.getUser();
            var password = currConnection.getPassword();
            if (host.isBlank() || zosmfport == 0) {
                throw new IllegalStateException("Error: Hostname or z/OSMF port value(s) missing\n" +
                        "Check configuration file and try again...");
            }

            var isAuthAttrsMissing = (username.equals(Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE) ||
                    password.equals(Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE));
            if (isAuthAttrsMissing) {
                if (username.equals(Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE)) {
                    terminal.println("Enter username for " + host);
                    username = PromptUtil.getPromptInfo("username:", false);
                }

                if (password.equals(Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE)) {
                    terminal.println("Enter password for " + host + "/" + username);
                    String confirmPassword = null;
                    while (confirmPassword == null || !confirmPassword.equals(password)) {
                        password = PromptUtil.getPromptInfo("password:", true);
                        confirmPassword = PromptUtil.getPromptInfo("confirm password:", true);
                    }
                }

                currConnection = ZosConnectionFactory.createBasicConnection(host, zosmfport, username, password);
                ConfigSingleton.getInstance().setZosConnectionByIndex(currConnection, 0);
                ConnSingleton.getInstance().setCurrZosConnection(currConnection, 0);
                currSshConnection = new SshConnection(host, currSshConnection.getPort(), username, password);
                ConfigSingleton.getInstance().setSshConnectionByIndex(currSshConnection, 0);
                ConnSingleton.getInstance().setCurrSshConnection(currSshConnection);
            }
            var msg = String.format("Connection defined:\nhost:%s\nuser:%s\nzosmfport:%s\nsshport:%s",
                    currConnection.getHost(), currConnection.getUser(), currConnection.getZosmfPort(),
                    currSshConnection.getPort());
            terminal.println(msg);
        } catch (Exception e) {
            var mainTerminal = TerminalSingleton.getInstance().getMainTerminal();
            mainTerminal.println("ERROR: Default connection invalid, try again...");
            mainTerminal.println("Error: " + e.getMessage());
            mainTerminal.read(true);
            mainTerminal.dispose();
            throw new RuntimeException(e);
        }

        var commandRouter = new CommandRouter(terminal);
        var historyResolver = new HistoryCommandResolver(terminal, HistorySingleton.getInstance().getHistory());
        while (true) {
            var input = textIO.newStringInputReader().withMaxLength(80).read(PromptUtil.getPrompt());
            if ("end".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                break;
            }

            if (isFontSizeChanged()) {
                terminal.println("Font size updated.");
                continue;
            }

            String[] tokens = StrUtil.stripEmptyStrings(input.trim().split("\\s+"));
            if (tokens.length == 0) continue;

            if (tokens[0].startsWith("!")) {
                tokens = historyResolver.resolve(tokens);
                if (tokens == null) continue;
            }

            if (tokens[0].equalsIgnoreCase(PromptUtil.getPrompt()) && tokens.length > 1) {
                tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
            }

            commandRouter.routeCommand(String.join(" ", tokens));
        }

        textIO.dispose();
    }

    private boolean isFontSizeChanged() {
        LOG.debug("*** isFontSizeChanged ***");
        if (TerminalSingleton.getInstance().isFontSizeChanged()) {
            TerminalSingleton.getInstance().setFontSizeChanged(false);
            return true;
        }
        return false;
    }

}
