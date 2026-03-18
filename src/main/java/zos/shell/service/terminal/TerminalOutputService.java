package zos.shell.service.terminal;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.singleton.ScreenBufferSingleton;

public class TerminalOutputService {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalOutputService.class);

    private final TextTerminal<?> terminal;
    private boolean redrawing;

    public TerminalOutputService(final TextTerminal<?> terminal) {
        LOG.debug("*** TerminalOutputService ***");
        this.terminal = terminal;
    }

    public void println(final String text) {
        LOG.debug("*** println ***");
        ScreenBufferSingleton.getInstance().addLine(text);
        terminal.println(text);
    }

    public void print(final String text) {
        LOG.debug("*** print ***");
        ScreenBufferSingleton.getInstance().addLine(text);
        terminal.print(text);
    }

    public void store(final String text) {
        LOG.debug("*** store ***");
        ScreenBufferSingleton.getInstance().addLine(text);
    }

    public void bufferMultilineAndPrint(final String text) {
        LOG.debug("*** bufferMultilineAndPrint ***");
        if (text == null) {
            return;
        }

        ScreenBufferSingleton.getInstance().addLines(text);
        terminal.println(text);
    }

    public void clear() {
        LOG.debug("*** clear ***");
        ScreenBufferSingleton.getInstance().clear();
    }

    public void redrawBufferedOutput() {
        LOG.debug("*** redrawBufferedOutput ***");
        if (redrawing) {
            return;
        }
        redrawing = true;

        try {
            terminal.resetToBookmark("top");
            var lines = ScreenBufferSingleton.getInstance().getLines();
            for (String line : lines) {
                terminal.println(line);
            }
        } finally {
            redrawing = false;
        }
    }

}
