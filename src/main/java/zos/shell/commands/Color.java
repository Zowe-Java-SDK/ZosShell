package zos.shell.commands;

import org.beryx.textio.TextTerminal;

public class Color {

    private final TextTerminal<?> terminal;

    public Color(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void setTextColor(String color) {
        terminal.getProperties().setPromptColor(color);
        terminal.getProperties().setInputColor(color);
        display("text color " + color + " set");
    }

    public void setBackGroundColor(String color) {
        if (color != null) {
            terminal.getProperties().setPaneBackgroundColor(color);
            display("background color " + color + " set");
        }
    }

    private void display(String msg) {
        terminal.println(msg);
    }

}
