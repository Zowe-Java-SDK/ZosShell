package com.commands;

import com.Constants;
import com.utility.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
import utility.UtilIO;
import zosfiles.ZosDsnDownload;
import zosfiles.input.DownloadParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Download {

    private final TextTerminal<?> terminal;
    private final ZosDsnDownload download;
    private final DownloadParams dlParams = new DownloadParams.Builder().build();

    public Download(TextTerminal<?> terminal, ZosDsnDownload download) {
        this.terminal = terminal;
        this.download = download;
    }

    public boolean download(String dataSet, String param) {
        try {
            var content = getContent(dataSet, param);
            if (content == null) {
                terminal.println(Constants.DOWNLOAD_FAIL);
                return false;
            }
            if (!SystemUtils.IS_OS_WINDOWS) {
                terminal.println(Constants.WINDOWS_ERROR_MSG);
                return false;
            }
            String pathAndFileName = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\" + param;
            Files.write(Paths.get(pathAndFileName), content.getBytes());
            terminal.println("downloaded to " + pathAndFileName);
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return false;
            }
            Util.printError(terminal, e.getMessage());
            return false;
        }
        return true;
    }

    public String getContent(String dataSet, String param) throws Exception {
        InputStream inputStream;
        if (Util.isDataSet(param)) {
            inputStream = download.downloadDsn(String.format("%s", param), dlParams);
        } else {
            inputStream = download.downloadDsn(String.format("%s(%s)", dataSet, param), dlParams);
        }
        return getStreamData(inputStream);
    }

    private String getStreamData(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, UtilIO.UTF8);
            var content = writer.toString();
            inputStream.close();
            return content;
        }
        return null;
    }

}
