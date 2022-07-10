package com.config;

import com.Constants;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class ColorConfig {

    public static void readConfig(TextTerminal<?> terminal) {
        File file = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(Constants.COLOR_CONFIG_PATH_FILE_WINDOWS);
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(Constants.COLOR_CONFIG_PATH_FILE_MAC);
        }
        String[] str = null;
        try (final var br = new BufferedReader(new FileReader(Objects.requireNonNull(file)))) {
            str = br.readLine().split(",");
        } catch (IOException | NullPointerException ignored) {
        }
        if (str != null) {
            terminal.getProperties().setPromptColor(str[0]);
            terminal.getProperties().setInputColor(str[0]);
            terminal.getProperties().setPaneBackgroundColor(str[1]);
        }
    }

}
