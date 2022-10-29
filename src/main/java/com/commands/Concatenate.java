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
        String result;
        try {
            inputStream = download.getInputStream(dataSet, param);
            result = retrieveInfo(inputStream);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }
        return new ResponseStatus(result, true);
    }

    private String retrieveInfo(final InputStream inputStream) throws IOException {
        if (inputStream != null) {
            final var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, Constants.UTF8);
            return writer.toString();
        }
        return null;
    }

}
