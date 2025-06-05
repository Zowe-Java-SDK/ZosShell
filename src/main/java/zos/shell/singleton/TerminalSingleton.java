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

import javax.swing.*;
import java.net.URL;
import java.util.List;

public class TerminalSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalSingleton.class);

    private static final SearchCommandService searchCommandService = new SearchCommandService();
    private static SwingTextTerminal mainTerminal;
    private static TextTerminal<?> terminal;
    private static TextIO mainTextIO;
    private static final int defaultFontSize = Constants.DEFAULT_FONT_SIZE;
    private static int fontSize = defaultFontSize;
    private static boolean fontSizeChanged = false;
    private static boolean disableKeys = false;

    private static class Holder {
        private static final TerminalSingleton instance = new TerminalSingleton();
    }

    private TerminalSingleton() {
        LOG.debug("*** TerminalSingleton ***");
    }

    public static TerminalSingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return TerminalSingleton.Holder.instance;
    }

    public SwingTextTerminal getMainTerminal() {
        LOG.debug("*** getMainTerminal ***");
        return mainTerminal;
    }

    public void setMainTerminal(final SwingTextTerminal mainTerminal) {
        LOG.debug("*** setMainTerminal ***");
        TerminalSingleton.mainTerminal = mainTerminal;
    }

    public TextTerminal<?> getTerminal() {
        LOG.debug("*** getTerminal ***");
        return terminal;
    }

    public void setTerminal(final TextTerminal<?> terminal) {
        LOG.debug("*** setTerminal ***");
        TerminalSingleton.terminal = terminal;
    }

    public TextIO getMainTextIO() {
        LOG.debug("*** getMainTextIO ***");
        return mainTextIO;
    }

    public void setMainTextIO(final TextIO mainTextIO) {
        LOG.debug("*** setMainTextIO ***");
        TerminalSingleton.mainTextIO = mainTextIO;
    }

    public void setFontSize(final int fontSize) {
        LOG.debug("*** setFontSize ***");
        TerminalSingleton.fontSize = fontSize;
    }

    public boolean isFontSizeChanged() {
        LOG.debug("*** isFontSizeChanged ***");
        return fontSizeChanged;
    }

    public void setFontSizeChanged(final boolean fontSizeChanged) {
        LOG.debug("*** setFontSizeChanged ***");
        TerminalSingleton.fontSizeChanged = fontSizeChanged;
    }

    public void setDisableKeys(final boolean disableKeys) {
        LOG.debug("*** setDisableKeys ***");
        TerminalSingleton.disableKeys = disableKeys;
    }

    public void setTerminalProperties() {
        LOG.debug("*** setTerminalProperties ***");
        var title = "";
        var zosConnection = ConnSingleton.getInstance().getCurrZosConnection();
        if (zosConnection != null) {
            title = " - " + zosConnection.getHost().toUpperCase();
        }
        mainTerminal.setPaneTitle(Constants.APP_TITLE + title);

        URL iconURL = ZosShell.class.getResource("/image/zowe-icon.png");
        if (iconURL != null) {
            var icon = new ImageIcon(iconURL);
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
            HistorySingleton.getInstance().getHistory().navigateHistory(HistoryService.NavigationDirection.UP);
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("DOWN", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            HistorySingleton.getInstance().getHistory().navigateHistory(HistoryService.NavigationDirection.DOWN);
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        if (SystemUtils.IS_OS_WINDOWS) {
            mainTerminal.registerHandler("ctrl UP", t -> {
                if (disableKeys) {
                    return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
                }
                fontSize++;
                mainTerminal.setInputFontSize(fontSize);
                mainTerminal.setPromptFontSize(fontSize);
                mainTerminal.moveToLineStart();
                mainTerminal.print(PromptUtil.getPrompt() + " Increased font size to " + fontSize + ".");
                fontSizeChanged = true;
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            });
            mainTerminal.registerHandler("ctrl DOWN", t -> {
                if (disableKeys) {
                    return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
                }
                if (fontSize != defaultFontSize) {
                    fontSize--;
                    mainTerminal.setInputFontSize(fontSize);
                    mainTerminal.setPromptFontSize(fontSize);
                    mainTerminal.moveToLineStart();
                    mainTerminal.print(PromptUtil.getPrompt() + " Decreased font size to " + fontSize + ".");
                    fontSizeChanged = true;
                }
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            });
        } else {
            mainTerminal.registerHandler("shift UP", t -> {
                if (disableKeys) {
                    return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
                }
                fontSize++;
                mainTerminal.setInputFontSize(fontSize);
                mainTerminal.setPromptFontSize(fontSize);
                mainTerminal.moveToLineStart();
                mainTerminal.print(PromptUtil.getPrompt() + " Increased font size to " + fontSize + ".");
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
                    mainTerminal.print(PromptUtil.getPrompt() + " Decreased font size to " + fontSize + ".");
                    fontSizeChanged = true;
                }
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            });
        }
        mainTerminal.registerHandler("TAB", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            var items = mainTerminal.getTextPane().getText().split(PromptUtil.getPrompt());
            var candidateStr = items[items.length - 1].trim();
            candidateStr = candidateStr.replaceAll("[\\p{Cf}]", "");
            if (candidateStr.contains(" ")) {  // invalid look up
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            boolean isAlphabetOnly = candidateStr.matches("[a-zA-Z]*");
            if (!isAlphabetOnly) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            List<String> candidateLst = searchCommandService.search(candidateStr);
            if (!candidateLst.isEmpty()) {
                mainTerminal.moveToLineStart();
                if (candidateLst.size() == 1) {
                    mainTerminal.replaceInput(candidateLst.get(0), false);
                } else {
                    mainTextIO.newStringInputReader().withDefaultValue("hit enter to skip")
                            .read((PromptUtil.getPrompt() + " " + candidateLst));
                }
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

}
