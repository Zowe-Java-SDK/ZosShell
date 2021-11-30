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
        String content = getContent(dataSet, param);
        try {
            Files.write(Paths.get(Constants.PATH_FILE + "\\" + param), content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getContent(String dataSet, String param) {
        InputStream inputStream;
        try {
            if (Util.isDataSet(param)) {
                inputStream = download.downloadDsn(String.format("%s", param), dlParams);
            } else {
                inputStream = download.downloadDsn(String.format("%s(%s)", dataSet, param), dlParams);
            }
            return getStreamData(inputStream);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.println(Constants.SEVERE_ERROR);
                return null;
            }
            Util.printError(terminal, e.getMessage());
        }
        return null;
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
