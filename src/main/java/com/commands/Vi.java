package com.commands;

import com.Constants;
import com.dto.ResponseStatus;
import com.utility.Util;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

public class Vi {

    private final Download download;
    private final Runtime rs = Runtime.getRuntime();

    public Vi(Download download) {
        this.download = download;
    }

    public ResponseStatus vi(String dataSet, String member) {
        var result = download.download(dataSet, member);
        var successMsg = "opening " + member + " in editor...";
        var errorMsg = "\ncannot open \" + member + \", try again...";
        try {
            if (result.isStatus()) {
                String pathFile;
                String editorName;
                if (SystemUtils.IS_OS_WINDOWS) {
                    pathFile = Download.DIRECTORY_PATH_WINDOWS + dataSet + "\\" + member;
                    editorName = Constants.WINDOWS_EDITOR_NAME;
                } else if (SystemUtils.IS_OS_MAC_OSX) {
                    pathFile = Download.DIRECTORY_PATH_MAC + dataSet + "/" + member;
                    editorName = Constants.MAC_EDITOR_NAME;
                } else {
                    return new ResponseStatus(Constants.OS_ERROR, false);
                }
                rs.exec(editorName + " " + pathFile);
            } else {
                return new ResponseStatus(Util.getMsgAfterArrow(result.getMessage()) + errorMsg, false);
            }
        } catch (IOException e) {
            return new ResponseStatus(Util.getMsgAfterArrow(e + "") + errorMsg, false);
        }
        return new ResponseStatus(successMsg, true);
    }

}
