package zos.shell.config;

import zos.shell.Constants;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TerminalProperties;
import org.beryx.textio.TextTerminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class Config {

    private final TextTerminal<?> terminal;
    private String frontSize;

    public Config(TextTerminal<?> terminal) {
        this.terminal = terminal;
        this.readConfig();
    }

    public String getFrontSize() {
        return frontSize;
    }

    public void readConfig() {
        File file = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(Constants.CONFIG_PATH_FILE_WINDOWS);
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(Constants.CONFIG_PATH_FILE_MAC);
        }
        String[] str = null;
        try (final var br = new BufferedReader(new FileReader(Objects.requireNonNull(file)))) {
            str = br.readLine().split(",");
        } catch (IOException | NullPointerException ignored) {
        }
        if (str != null) {
            try {
                if (str[0] != null) {
                    final var textColor = str[0];
                    terminal.getProperties().setPromptColor(textColor);
                    terminal.getProperties().setInputColor(textColor);
                }
                if (str[1] != null) {
                    final var backGroundColor = str[1];
                    terminal.getProperties().setPaneBackgroundColor(backGroundColor);
                }
                TerminalProperties<?> tp = terminal.getProperties();
                if (str[2] != null) {
                    frontSize = str[2];
                    tp.put("prompt.font.size", Integer.valueOf(frontSize));
                    tp.put("input.font.size", Integer.valueOf(frontSize));
                }
                if (str[3] != null) {
                    tp.put("prompt.bold", true);
                    tp.put("input.bold", true);
                }
            } catch (Exception ignored) {
            }
        }
    }

}
