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

    public void vi(String dataSet, String member) {
        DownloadStatus result = download.download(dataSet, member);
        try {
            if (result.isStatus() && SystemUtils.IS_OS_WINDOWS) {
                var pathFile = Download.DIRECTORY_PATH + dataSet + "//" + member;
                var editor = Constants.WINDOWS_EDITOR_NAME;
                rs.exec(editor + " " + pathFile);
            }
            if (!SystemUtils.IS_OS_WINDOWS) {
                result = new DownloadStatus(Constants.WINDOWS_ERROR_MSG, false);
            }
        } catch (IOException e) {
            result = new DownloadStatus(e.getMessage(), false);
        }
        if (!result.isStatus()) {
            final var message = result.getMessage();
            var index = message.indexOf(Constants.ARROW) + Constants.ARROW.length();
            terminal.println(result.getMessage().substring(index));
            terminal.println("cannot open " + member + ", try again...");
        } else {
            terminal.println("opening in editor...");
        }
    }

}
