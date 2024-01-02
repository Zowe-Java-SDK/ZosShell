package zos.shell.service.dsn.concatenate;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.DownloadCmd;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class Concat {

    private static final Logger LOG = LoggerFactory.getLogger(Concat.class);

    private final DownloadCmd download;

    public Concat(DownloadCmd download) {
        LOG.debug("*** Concat ***");
        this.download = download;
    }

    public ResponseStatus cat(final String dataset, final String target) {
        LOG.debug("*** cat ***");
        InputStream inputStream;
        String result;
        try {
            if (Util.isMember(target)) {
                // retrieve member data
                inputStream = download.getInputStream(String.format("%s(%s)", dataset, target));
            } else {
                // retrieve sequential dataset data
                inputStream = download.getInputStream(target);
            }
            result = retrieveInfo(inputStream);
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        } catch (IOException e) {
            return new ResponseStatus(e.getMessage(), false);
        }
        return new ResponseStatus(result != null ? result : "no data to display", true);
    }

    private String retrieveInfo(final InputStream inputStream) throws IOException {
        if (inputStream != null) {
            final var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, Constants.UTF8);
            inputStream.close();
            return writer.toString();
        }
        return null;
    }

}
