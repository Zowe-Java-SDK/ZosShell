package zos.shell.singleton;

import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import zos.shell.ZosShell;
import zos.shell.constants.Constants;
import zos.shell.service.autocomplete.SearchCommandService;
import zos.shell.utility.PromptUtil;

import javax.swing.*;
import java.net.URL;
import java.util.List;

public class TerminalSingleton {

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
    }

    public static TerminalSingleton getInstance() {
        return TerminalSingleton.Holder.instance;
    }

    public SwingTextTerminal getMainTerminal() {
        return mainTerminal;
    }

    public void setMainTerminal(final SwingTextTerminal mainTerminal) {
        TerminalSingleton.mainTerminal = mainTerminal;
    }

    public TextTerminal<?> getTerminal() {
        return terminal;
    }

    public void setTerminal(final TextTerminal<?> terminal) {
        TerminalSingleton.terminal = terminal;
    }

    public TextIO getMainTextIO() {
        return mainTextIO;
    }

    public void setMainTextIO(final TextIO mainTextIO) {
        TerminalSingleton.mainTextIO = mainTextIO;
    }

    public void setFontSize(final int fontSize) {
        TerminalSingleton.fontSize = fontSize;
    }

    public boolean isFontSizeChanged() {
        return fontSizeChanged;
    }

    public void setFontSizeChanged(final boolean fontSizeChanged) {
        TerminalSingleton.fontSizeChanged = fontSizeChanged;
    }

    public void setDisableKeys(final boolean disableKeys) {
        TerminalSingleton.disableKeys = disableKeys;
    }

    public void setTerminalProperties() {
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
            HistorySingleton.getInstance().getHistory().listUpCommands(PromptUtil.getPrompt());
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
        mainTerminal.registerHandler("DOWN", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            HistorySingleton.getInstance().getHistory().listDownCommands(PromptUtil.getPrompt());
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
        mainTerminal.registerHandler("TAB", t -> {
            if (disableKeys) {
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            String[] items = mainTerminal.getTextPane().getText().split(PromptUtil.getPrompt());
            var candidateStr = items[items.length - 1].trim();
            candidateStr = candidateStr.replaceAll("[\\p{Cf}]", "");
            if (candidateStr.contains(" ")) {  // invalid look up
                return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
            }
            List<String> candidateLst = searchCommandService.search(candidateStr);
            if (!candidateLst.isEmpty()) {
                mainTerminal.moveToLineStart();
                if (candidateLst.size() == 1) {
                    mainTerminal.print(PromptUtil.getPrompt() + " " + candidateLst.get(0));
                } else {
                    mainTextIO.newStringInputReader().withDefaultValue("hit enter to skip")
                            .read((PromptUtil.getPrompt() + " " + candidateLst));
                }
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

}
