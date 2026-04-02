package zos.shell;

import org.beryx.textio.TextIO;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.connection.ConnectionStartupService;
import zos.shell.singleton.HistorySingleton;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.singleton.configuration.ConfigSingleton;
import zos.shell.state.ShellStateMachine;

import java.io.IOException;
import java.util.function.BiConsumer;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final Logger LOG = LoggerFactory.getLogger(ZosShell.class);

    private final String connectionArgument;

    public ZosShell(final String connectionArgument) {
        this.connectionArgument = connectionArgument;
    }

    public static void main(String[] args) {
        LOG.debug("*** main ***");
        var terminalSingleton = TerminalSingleton.getInstance();
        var connectionIdentifier = args.length > 0 ? args[0] : "";
        initializeTerminal(terminalSingleton, connectionIdentifier);
        new ZosShell(connectionIdentifier).accept(terminalSingleton.getMainTextIO(), null);
    }

    @Override
    public void accept(final TextIO textIO, final RunnerData runnerData) {
        LOG.debug("*** accept ***");
        var terminalSingleton = TerminalSingleton.getInstance();
        var configSingleton = ConfigSingleton.getInstance();

        initializeInteractiveTerminal(textIO, terminalSingleton, configSingleton);
        initializeHistory(terminalSingleton);
        try {
            new ConnectionStartupService().initialize(connectionArgument, textIO.getTextTerminal());
        } catch (Exception e) {
            failAndExit(e);
        }

        new ShellStateMachine(textIO).run();
        textIO.dispose();
    }

    private static void initializeTerminal(final TerminalSingleton terminalSingleton,
                                           final String connectionIdentifier) {
        LOG.debug("*** initializeTerminal ***");
        var mainTerminal = new SwingTextTerminal();
        terminalSingleton.setMainTerminal(mainTerminal);
        terminalSingleton.setTerminalProperties();
        readConfiguration(terminalSingleton, connectionIdentifier);

        var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        try {
            int paneWidth = Integer.parseInt(configSettings.getWindow().getPaneWidth());
            int paneHeight = Integer.parseInt(configSettings.getWindow().getPaneHeight());

            mainTerminal.getProperties().setPaneWidth(paneWidth);
            mainTerminal.getProperties().setPaneHeight(paneHeight);
        } catch (NumberFormatException e) {
            LOG.debug("Invalid pane size width / height", e);
        }

        mainTerminal.init();
        terminalSingleton.setMainTextIO(new TextIO(mainTerminal));

        applyConfiguredFontSize(terminalSingleton);
    }

    private static void readConfiguration(final TerminalSingleton terminalSingleton,
                                          final String connectionIdentifier) {
        LOG.debug("*** readConfiguration ***");
        try {
            ConfigSingleton.getInstance().readConfig(connectionIdentifier);
        } catch (NumberFormatException | IOException e) {
            var mainTerminal = terminalSingleton.getMainTerminal();
            mainTerminal.println("Error reading or parsing config.json file, try again...");
            mainTerminal.println("Error: " + e.getMessage());
            mainTerminal.read(true);
            mainTerminal.dispose();
            throw new RuntimeException(e);
        }
    }

    private static void applyConfiguredFontSize(final TerminalSingleton terminalSingleton) {
        LOG.debug("*** applyConfiguredFontSize ***");
        var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        if (configSettings == null) {
            return;
        }

        var window = configSettings.getWindow();
        if (window != null && window.getFontsize() != null) {
            int fontSize = Integer.parseInt(window.getFontsize());
            terminalSingleton.setFontSize(fontSize);
        }
    }

    private static void initializeInteractiveTerminal(final TextIO textIO,
                                                      final TerminalSingleton terminalSingleton,
                                                      final ConfigSingleton configSingleton) {
        LOG.debug("*** initializeInteractiveTerminal ***");
        terminalSingleton.setTerminal(textIO.getTextTerminal());
        terminalSingleton.getTerminal().setBookmark("top");
        configSingleton.updateWindowSettings(terminalSingleton.getTerminal());
    }

    private static void initializeHistory(final TerminalSingleton terminalSingleton) {
        LOG.debug("*** initializeHistory ***");
        HistorySingleton.getInstance().setHistory(terminalSingleton.getTerminal());
    }

    private static void failAndExit(final Exception e) {
        LOG.debug("*** failAndExit ***");
        var mainTerminal = TerminalSingleton.getInstance().getMainTerminal();
        mainTerminal.println("ERROR: Default connection invalid, try again...");
        mainTerminal.println("Error: " + e.getMessage());
        LOG.error("Default connection invalid.", e);
        mainTerminal.read(true);
        mainTerminal.dispose();
        throw new RuntimeException(e);
    }

}
