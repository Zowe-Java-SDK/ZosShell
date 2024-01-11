package zos.shell.service.change;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowCmd {

    private static final Logger LOG = LoggerFactory.getLogger(WindowCmd.class);

    private final TextTerminal<?> terminal;

    public WindowCmd(final TextTerminal<?> terminal) {
        LOG.debug("*** Color ***");
        this.terminal = terminal;
    }

    public void setTextColor(final String color) {
        LOG.debug("*** setTextColor ***");
        if (color != null) {
            terminal.getProperties().setPromptColor(color);
            terminal.getProperties().setInputColor(color);
            display("text color " + color + " set");
        }
    }

    public void setBackGroundColor(final String color) {
        LOG.debug("*** setBackGroundColor ***");
        if (color != null) {
            terminal.getProperties().setPaneBackgroundColor(color);
            display("background color " + color + " set");
        }
    }

    public void setBold(boolean value) {
        LOG.debug("*** setBold ***");
        final var tp = terminal.getProperties();
        tp.put("prompt.bold", value);
        tp.put("input.bold", value);
    }

    public void setFontSize(final String size) {
        if (size != null) {
            final var tp = terminal.getProperties();
            tp.put("prompt.font.size", Integer.valueOf(size));
            tp.put("input.font.size", Integer.valueOf(size));
        }
    }

    private void display(final String msg) {
        LOG.debug("*** display ***");
        terminal.println(msg);
    }

}
