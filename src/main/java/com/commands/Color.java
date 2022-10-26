package com.commands;

import org.beryx.textio.TextTerminal;

public class Color {

    private final TextTerminal<?> terminal;

    public Color(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void setTextColor(String color) {
        terminal.getProperties().setPromptColor(color);
        terminal.getProperties().setInputColor(color);
        display();
    }

    public void setBackGroundColor(String color) {
        if (color != null) {
            terminal.getProperties().setPaneBackgroundColor(color);
            display();
        }
    }

    private void display() {
        terminal.println("color set");
    }

}
