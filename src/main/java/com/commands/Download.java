package com.commands;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.apache.commons.io.IOUtils;
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

    public Download(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.download = new ZosDsnDownload(connection);
    }

    public void download(String dataSet, String param) {
        try {
            String content = getContent(dataSet, param);
            if (content == null) {
                terminal.println(Constants.DOWNLOAD_FAIL);
                return;
            }
            String pathAndFileName = Constants.PATH_FILE_DIRECTORY + "\\" + param;
            Files.write(Paths.get(pathAndFileName), content.getBytes());
            terminal.println("downloaded to " + pathAndFileName);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
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
