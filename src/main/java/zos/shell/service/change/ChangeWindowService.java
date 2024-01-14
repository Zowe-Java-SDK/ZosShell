package zos.shell.service.change;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeWindowService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeWindowService.class);

    private final TextTerminal<?> terminal;

    public ChangeWindowService(final TextTerminal<?> terminal) {
        LOG.debug("*** ChangeWindowService ***");
        this.terminal = terminal;
    }

    public String setTextColor(final String color) {
        LOG.debug("*** setTextColor ***");
        if (color != null) {
            terminal.getProperties().setPromptColor(color);
            terminal.getProperties().setInputColor(color);
            return "text color " + color + " set";
        }
        return null;
    }

    public String setBackGroundColor(final String color) {
        LOG.debug("*** setBackGroundColor ***");
        if (color != null) {
            terminal.getProperties().setPaneBackgroundColor(color);
            return "background color " + color + " set";
        }
        return null;
    }

    public String setBold(boolean value) {
        LOG.debug("*** setBold ***");
        final var tp = terminal.getProperties();
        tp.put("prompt.bold", value);
        tp.put("input.bold", value);
        if (value) {
            return "font bold set";
        }
        return null;
    }

    public String setFontSize(final String size) {
        LOG.debug("*** setFontSize ***");
        if (size != null) {
            final var tp = terminal.getProperties();
            tp.put("prompt.font.size", Integer.valueOf(size));
            tp.put("input.font.size", Integer.valueOf(size));
            return "increased font size to " + size;
        }
        return null;
    }

}

