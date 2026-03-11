package zos.shell.singleton;

import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.ZosShell;
import zos.shell.constants.Constants;
import zos.shell.service.autocomplete.SearchCommandService;
import zos.shell.service.history.HistoryService;
import zos.shell.utility.PromptUtil;

import javax.swing.ImageIcon;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

public final class TerminalSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalSingleton.class);

    private static final Pattern ALPHABETIC_PATTERN = Pattern.compile("[a-zA-Z]*");
    private static final SearchCommandService SEARCH_COMMAND_SERVICE = new SearchCommandService();

    private final int defaultFontSize = Constants.DEFAULT_FONT_SIZE;

    private SwingTextTerminal mainTerminal;
    private TextTerminal<?> terminal;
    private TextIO mainTextIO;
    private int fontSize = defaultFontSize;
    private boolean fontSizeChanged;
    private boolean disableKeys;

    private static class Holder {
        private static final TerminalSingleton INSTANCE = new TerminalSingleton();
    }

    private TerminalSingleton() {
        LOG.debug("*** TerminalSingleton ***");
    }

    public static TerminalSingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return Holder.INSTANCE;
    }

    public SwingTextTerminal getMainTerminal() {
        LOG.debug("*** getMainTerminal ***");
        return mainTerminal;
    }

    public void setMainTerminal(final SwingTextTerminal mainTerminal) {
        LOG.debug("*** setMainTerminal ***");
        this.mainTerminal = mainTerminal;
    }

    public TextTerminal<?> getTerminal() {
        LOG.debug("*** getTerminal ***");
        return terminal;
    }

    public void setTerminal(final TextTerminal<?> terminal) {
        LOG.debug("*** setTerminal ***");
        this.terminal = terminal;
    }

    public TextIO getMainTextIO() {
        LOG.debug("*** getMainTextIO ***");
        return mainTextIO;
    }

    public void setMainTextIO(final TextIO mainTextIO) {
        LOG.debug("*** setMainTextIO ***");
        this.mainTextIO = mainTextIO;
    }

    public int getFontSize() {
        LOG.debug("*** getFontSize ***");
        return fontSize;
    }

    public void setFontSize(final int fontSize) {
        LOG.debug("*** setFontSize ***");
        this.fontSize = fontSize;
    }

    public boolean isFontSizeChanged() {
        LOG.debug("*** isFontSizeChanged ***");
        return fontSizeChanged;
    }

    public void setFontSizeChanged(final boolean fontSizeChanged) {
        LOG.debug("*** setFontSizeChanged ***");
        this.fontSizeChanged = fontSizeChanged;
    }

    public boolean isDisableKeys() {
        LOG.debug("*** isDisableKeys ***");
        return disableKeys;
    }

    public void setDisableKeys(final boolean disableKeys) {
        LOG.debug("*** setDisableKeys ***");
        this.disableKeys = disableKeys;
    }

    public void setTerminalProperties() {
        LOG.debug("*** setTerminalProperties ***");

        if (mainTerminal == null) {
            throw new IllegalStateException("Main terminal must be initialized before setting terminal properties.");
        }

        setPaneTitle();
        setApplicationIcon();
        registerHandlers();
    }

    private void setPaneTitle() {
        String titleSuffix = "";
        var zosConnection = ConnSingleton.getInstance().getCurrZosConnection();
        if (zosConnection != null) {
            titleSuffix = " - " + zosConnection.getHost().toUpperCase();
        }
        mainTerminal.setPaneTitle(Constants.APP_TITLE + titleSuffix);
    }

    private void setApplicationIcon() {
        URL iconUrl = ZosShell.class.getResource("/image/zowe-icon.png");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            mainTerminal.getFrame().setIconImage(icon.getImage());
        }
    }

    private void registerHandlers() {
        registerCopyHandler();
        registerHistoryHandlers();
        registerFontResizeHandlers();
        registerAutocompleteHandler();
    }

    private void registerCopyHandler() {
        mainTerminal.registerHandler("ctrl C", terminal -> {
            terminal.getTextPane().copy();
            return continueAction();
        });
    }

    private void registerHistoryHandlers() {
        mainTerminal.registerHandler("UP", terminal -> {
            if (disableKeys) {
                return continueAction();
            }
            HistorySingleton.getInstance()
                    .getHistory()
                    .navigateHistory(HistoryService.NavigationDirection.UP);
            return continueAction();
        });

        mainTerminal.registerHandler("DOWN", terminal -> {
            if (disableKeys) {
                return continueAction();
            }
            HistorySingleton.getInstance()
                    .getHistory()
                    .navigateHistory(HistoryService.NavigationDirection.DOWN);
            return continueAction();
        });
    }

    private void registerFontResizeHandlers() {
        if (SystemUtils.IS_OS_WINDOWS) {
            mainTerminal.registerHandler("ctrl UP", terminal -> adjustFontSize(+1));
            mainTerminal.registerHandler("ctrl DOWN", terminal -> adjustFontSize(-1));
        } else {
            mainTerminal.registerHandler("shift UP", terminal -> adjustFontSize(+1));
            mainTerminal.registerHandler("shift DOWN", terminal -> adjustFontSize(-1));
        }
    }

    private ReadHandlerData adjustFontSize(final int delta) {
        if (disableKeys) {
            return continueAction();
        }

        int newFontSize = fontSize + delta;
        if (newFontSize < defaultFontSize) {
            return continueAction();
        }

        fontSize = newFontSize;
        mainTerminal.setInputFontSize(fontSize);
        mainTerminal.setPromptFontSize(fontSize);
        mainTerminal.moveToLineStart();

        String action = delta > 0 ? "Increased" : "Decreased";
        mainTerminal.print(PromptUtil.getPrompt() + " " + action + " font size to " + fontSize + ".");
        fontSizeChanged = true;

        return continueAction();
    }

    private void registerAutocompleteHandler() {
        mainTerminal.registerHandler("TAB", terminal -> {
            if (disableKeys) {
                return continueAction();
            }

            String candidate = extractAutocompleteCandidate();
            if (!isValidAutocompleteCandidate(candidate)) {
                return continueAction();
            }

            List<String> candidates = SEARCH_COMMAND_SERVICE.search(candidate);
            if (candidates.isEmpty()) {
                return continueAction();
            }

            mainTerminal.moveToLineStart();
            if (candidates.size() == 1) {
                mainTerminal.replaceInput(candidates.get(0), false);
            } else {
                mainTextIO.newStringInputReader()
                        .withDefaultValue("hit enter to skip")
                        .read(PromptUtil.getPrompt() + " " + candidates);
            }

            return continueAction();
        });
    }

    private String extractAutocompleteCandidate() {
        String[] items = mainTerminal.getTextPane().getText().split(Pattern.quote(PromptUtil.getPrompt()));
        String candidate = items[items.length - 1].trim();
        return candidate.replaceAll("[\\p{Cf}]", "");
    }

    private boolean isValidAutocompleteCandidate(final String candidate) {
        if (candidate.isEmpty()) {
            return true;
        }
        if (candidate.contains(" ")) {
            return false;
        }
        return ALPHABETIC_PATTERN.matcher(candidate).matches();
    }

    private ReadHandlerData continueAction() {
        return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
    }

}
