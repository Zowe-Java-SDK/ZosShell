package zos.shell.service.dsn.concat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.Download;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FileUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;

import java.io.IOException;
import java.io.InputStream;

public class Concat {

    private static final Logger LOG = LoggerFactory.getLogger(Concat.class);

    private final Download download;

    public Concat(final Download download) {
        LOG.debug("*** Concat ***");
        this.download = download;
    }

    public ResponseStatus cat(final String dataset, final String target) {
        LOG.debug("*** cat ***");
        InputStream inputStream;
        String result;
        try {
            if (DsnUtil.isMember(target)) {
                // retrieve member data
                inputStream = download.getInputStream(String.format("%s(%s)", dataset, target));
            } else {
                // retrieve sequential dataset data
                inputStream = download.getInputStream(target);
            }
            result = FileUtil.getTextStreamData(inputStream);
            return new ResponseStatus(result != null ? result : "no data to display", true);
        } catch (ZosmfRequestException e) {
            return ResponseUtil.getByteResponseStatus(e);
        } catch (IOException e) {
            return new ResponseStatus(e.getMessage(), false);
        }
    }

}
