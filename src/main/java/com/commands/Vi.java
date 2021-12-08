package com.commands;

import com.Constants;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;

import java.io.IOException;

public class Vi {

    private final TextTerminal<?> terminal;
    private final Download download;
    private final Runtime rs = Runtime.getRuntime();

    public Vi(TextTerminal<?> terminal, Download download) {
        this.terminal = terminal;
        this.download = download;
    }

    public void vi(String dataSet, String param) {
        var success = download.download(dataSet, param);
        try {
            if (success && SystemUtils.IS_OS_WINDOWS)
                rs.exec("notepad " + Constants.PATH_FILE_DIRECTORY + "\\" + param);
            if (!SystemUtils.IS_OS_WINDOWS) {
                terminal.println(Constants.WINDOWS_ERROR_MSG);
            }
        } catch (IOException e) {
            terminal.println(e.getMessage());
        }
        if (!success) terminal.println("cannot open " + param + ", try again...");
    }

}
