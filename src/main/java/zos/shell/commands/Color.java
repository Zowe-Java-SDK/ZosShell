package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Color {

    private static final Logger LOG = LoggerFactory.getLogger(Color.class);

    private final TextTerminal<?> terminal;

    public Color(TextTerminal<?> terminal) {
        LOG.debug("*** Color ***");
        this.terminal = terminal;
    }

    public void setTextColor(String color) {
        LOG.debug("*** setTextColor ***");
        terminal.getProperties().setPromptColor(color);
        terminal.getProperties().setInputColor(color);
        display("text color " + color + " set");
    }

    public void setBackGroundColor(String color) {
        LOG.debug("*** setBackGroundColor ***");
        if (color != null) {
            terminal.getProperties().setPaneBackgroundColor(color);
            display("background color " + color + " set");
        }
    }

    private void display(String msg) {
        LOG.debug("*** display ***");
        terminal.println(msg);
    }

}
