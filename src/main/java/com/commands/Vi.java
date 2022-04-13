package com.commands;

import com.Constants;
import com.dto.ResponseStatus;
import com.utility.Util;
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
        ResponseStatus result = download.download(dataSet, member);
        try {
            if (result.isStatus()) {
                String pathFile = null;
                String editorName = null;
                if (SystemUtils.IS_OS_WINDOWS) {
                    pathFile = Download.DIRECTORY_PATH_WINDOWS + dataSet + "\\" + member;
                    editorName = Constants.WINDOWS_EDITOR_NAME;
                } else if (SystemUtils.IS_OS_MAC_OSX) {
                    pathFile = Download.DIRECTORY_PATH_MAC + dataSet + "/" + member;
                    editorName = Constants.MAC_EDITOR_NAME;
                }
                rs.exec(editorName + " " + pathFile);
            }
        } catch (IOException e) {
            result = new ResponseStatus(e.getMessage(), false);
        }
        if (!result.isStatus()) {
            terminal.println(Util.getMsgAfterArrow(result.getMessage()));
            terminal.println("cannot open " + member + ", try again...");
        } else {
            terminal.println("opening in editor...");
        }
    }

}
