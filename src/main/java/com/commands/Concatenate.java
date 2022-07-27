package com.commands;

import com.Constants;
import com.dto.ResponseStatus;
import com.utility.Util;
import org.apache.commons.io.IOUtils;
import org.beryx.textio.TextTerminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;

public class Concatenate {

    private final TextTerminal<?> terminal;
    private final Download download;

    public Concatenate(TextTerminal<?> terminal, Download download) {
        this.terminal = terminal;
        this.download = download;
    }

    public ResponseStatus cat(String dataSet, String param) {
        InputStream inputStream;
        try {
            inputStream = download.getInputStream(dataSet, param);
            display(inputStream);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }
        return new ResponseStatus("", true);
    }

    private void display(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            final var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, Constants.UTF8);
            final var content = writer.toString().split("\\n");
            Arrays.stream(content).forEach(terminal::println);
            inputStream.close();
        }
    }

}
