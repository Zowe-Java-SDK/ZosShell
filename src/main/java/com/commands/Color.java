package com.commands;

import org.beryx.textio.TextTerminal;

public class Color {

    private final TextTerminal<?> terminal;

    public Color(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void color(String color) {
        terminal.getProperties().setPromptColor(color);
        terminal.getProperties().setInputColor(color);
    }

}
