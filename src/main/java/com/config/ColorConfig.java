package com.config;

import com.Constants;
import org.beryx.textio.TextTerminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ColorConfig {

    public static void readConfig(TextTerminal<?> terminal) {
        var file = new File(Constants.COLOR_CONFIG_PATH_FILE);
        String[] str = null;
        try (var br = new BufferedReader(new FileReader(file))) {
            str = br.readLine().split(",");
        } catch (IOException ignored) {
        }
        if (str != null) {
            terminal.getProperties().setPromptColor(str[0]);
            terminal.getProperties().setInputColor(str[0]);
            terminal.getProperties().setPaneBackgroundColor(str[1]);
        }
    }

}
