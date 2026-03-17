package zos.shell.service.change;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeWinService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeWinService.class);

    private final TextTerminal<?> terminal;
    private final Runnable refreshScreenAction;

    private boolean currentBold = false;
    private Integer currentFontSize;
    private String currentTextColor;
    private String currentBackgroundColor;

    public ChangeWinService(final TextTerminal<?> terminal, final Runnable refreshScreenAction) {
        LOG.debug("*** ChangeWinService ***");
        this.terminal = terminal;
        this.refreshScreenAction = refreshScreenAction;
    }

    public String setTextColor(final String color) {
        LOG.debug("*** setTextColor ***");
        if (color == null || color.isBlank()) {
            return null;
        }

        currentTextColor = normalizeColor(color);
        reapplyCurrentSettings();
        refreshScreenIfSupported();

        return "text color " + currentTextColor + " set";
    }

    public String setBackGroundColor(final String color) {
        LOG.debug("*** setBackGroundColor ***");
        if (color == null || color.isBlank()) {
            return null;
        }

        currentBackgroundColor = normalizeColor(color);
        reapplyCurrentSettings();
        refreshScreenIfSupported();

        return "background color " + currentBackgroundColor + " set";
    }

    public String setBold(final boolean value) {
        LOG.debug("*** setBold ***");
        currentBold = value;
        reapplyCurrentSettings();
        refreshScreenIfSupported();

        return value ? "font bold set" : "font bold unset";
    }

    public String setFontSize(final String size) {
        LOG.debug("*** setFontSize ***");
        if (size == null || size.isBlank()) {
            return null;
        }

        try {
            currentFontSize = Integer.parseInt(size);
            reapplyCurrentSettings();
            refreshScreenIfSupported();
            return "font size set to " + size;
        } catch (NumberFormatException e) {
            LOG.debug("Invalid font size: {}", size, e);
            return "invalid font size";
        }
    }

    public String setPaneHeight(final String height) {
        LOG.debug("*** setPaneHeight ***");
        if (height == null || height.isBlank()) {
            return null;
        }

        try {
            terminal.getProperties().setPaneHeight(Integer.parseInt(height));
            return "pane height " + height + " set";
        } catch (NumberFormatException e) {
            LOG.debug("Invalid pane height: {}", height, e);
            return "invalid pane height";
        }
    }

    public String setPaneWidth(final String width) {
        LOG.debug("*** setPaneWidth ***");
        if (width == null || width.isBlank()) {
            return null;
        }

        try {
            terminal.getProperties().setPaneWidth(Integer.parseInt(width));
            return "pane width " + width + " set";
        } catch (NumberFormatException e) {
            LOG.debug("Invalid pane width: {}", width, e);
            return "invalid pane width";
        }
    }

    private void reapplyCurrentSettings() {
        if (currentTextColor != null) {
            terminal.getProperties().setPromptColor(currentTextColor);
            terminal.getProperties().setInputColor(currentTextColor);
        }

        if (currentBackgroundColor != null) {
            terminal.getProperties().setPaneBackgroundColor(currentBackgroundColor);
        }

        terminal.getProperties().put("prompt.bold", currentBold);
        terminal.getProperties().put("input.bold", currentBold);

        if (currentFontSize != null) {
            terminal.getProperties().put("prompt.font.size", currentFontSize);
            terminal.getProperties().put("input.font.size", currentFontSize);
        }
    }

    private void refreshScreenIfSupported() {
        if (refreshScreenAction == null) {
            return;
        }

        try {
            refreshScreenAction.run();
        } catch (RuntimeException e) {
            LOG.debug("Unable to refresh screen after style change", e);
        }
    }

    private String normalizeColor(final String color) {
        if (color == null) {
            return null;
        }

        switch (color.trim().toLowerCase()) {
            case "black":
                return "#000000";
            case "blue":
                return "#0000FF";
            case "cyan":
                return "#00FFFF";
            case "darkgray":
            case "dark_gray":
                return "#404040";
            case "gray":
                return "#808080";
            case "green":
                return "#006400";
            case "lightgray":
            case "light_gray":
                return "#C0C0C0";
            case "magenta":
                return "#FF00FF";
            case "orange":
                return "#FFA500";
            case "pink":
                return "#FFC0CB";
            case "red":
                return "#FF0000";
            case "white":
                return "#FFFFFF";
            case "yellow":
                return "#FFFF00";
            default:
                return color;
        }
    }

}
