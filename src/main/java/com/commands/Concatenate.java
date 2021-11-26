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
import java.util.Arrays;

public class Concatenate {

    private final TextTerminal<?> terminal;
    private final ZosDsnDownload dl;
    private final DownloadParams dlParams = new DownloadParams.Builder().build();

    public Concatenate(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        dl = new ZosDsnDownload(connection);
    }

    public void cat(String dataSet, String param) {
        InputStream inputStream;
        try {
            if (Util.isDataSet(param)) {
                inputStream = dl.downloadDsn(String.format("%s", param), dlParams);
            } else {
                inputStream = dl.downloadDsn(String.format("%s(%s)", dataSet, param), dlParams);
            }
            display(inputStream);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
    }

    private void display(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, UtilIO.UTF8);
            String[] content = writer.toString().split("\\n");
            Arrays.stream(content).forEach(terminal::println);
        }
        inputStream.close();
    }

}
