package zos.shell.commands;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class Concatenate {

    private static Logger LOG = LoggerFactory.getLogger(Concatenate.class);

    private final Download download;

    public Concatenate(Download download) {
        LOG.debug("*** Concatenate ***");
        this.download = download;
    }

    public ResponseStatus cat(String dataSet, String param) {
        LOG.debug("*** cat ***");
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
