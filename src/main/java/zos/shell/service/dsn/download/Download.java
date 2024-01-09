package zos.shell.service.dsn.download;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.DirectorySetup;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FileUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.DownloadParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.io.IOException;
import java.io.InputStream;

public class Download {

    private static final Logger LOG = LoggerFactory.getLogger(Download.class);

    private final DsnGet dsnGet;
    private DownloadParams dlParams;
    private final boolean isBinary;

    public Download(final DsnGet dsnGet, boolean isBinary) {
        LOG.debug("*** Download ***");
        this.dsnGet = dsnGet;
        this.isBinary = isBinary;
    }

    public ResponseStatus member(final String dataset, final String member) {
        LOG.debug("*** member ***");
        if (!DsnUtil.isMember(member)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }
        var message = Strings.padStart(member, Constants.STRING_PAD_LENGTH, ' ') + Constants.ARROW;

        final var dirSetup = new DirectorySetup();
        try {
            dirSetup.initialize(dataset, member);
        } catch (IllegalStateException e) {
            return new ResponseStatus(message + e.getMessage(), false);
        }

        try {
            String textContent;
            InputStream binaryContent;

            if (!isBinary) {
                dlParams = new DownloadParams.Builder().build();
                textContent = getTextContent(dataset, member);
                if (textContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                FileUtil.writeTextFile(textContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
            } else {
                dlParams = new DownloadParams.Builder().binary(true).build();
                binaryContent = getBinaryContent(dataset, member);
                if (binaryContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                FileUtil.writeBinaryFile(binaryContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
            }
            message += "downloaded to " + dirSetup.getFileNamePath();
        } catch (ZosmfRequestException e) {
            return ResponseUtil.getByteResponseStatus(e);
        } catch (IOException e) {
            return new ResponseStatus(message + e.getMessage(), false);
        }
        return new ResponseStatus(message, true, dirSetup.getFileNamePath());
    }

    public ResponseStatus dataset(final String dataset) {
        LOG.debug("*** dataset ***");
        var message = dataset + " " + Constants.ARROW;
        final var dirSetup = new DirectorySetup();

        try {
            dirSetup.initialize(Constants.SEQUENTIAL_DIRECTORY_LOCATION, dataset);
        } catch (IllegalStateException e) {
            return new ResponseStatus(message + e.getMessage(), false);
        }

        try {
            dlParams = new DownloadParams.Builder().build();
            String textContent = getTextContent(dataset);
            if (textContent == null) {
                return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
            }
            FileUtil.writeTextFile(textContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());

        } catch (ZosmfRequestException e) {
            return ResponseUtil.getByteResponseStatus(e);
        } catch (IOException e) {
            return new ResponseStatus(message + e.getMessage(), false);
        }

        message += "downloaded to " + dirSetup.getFileNamePath();
        return new ResponseStatus(message, true, dirSetup.getFileNamePath());
    }

    private String getTextContent(final String dataset, final String member)
            throws ZosmfRequestException, IOException {
        LOG.debug("*** getTextContent member ***");
        final var inputStream = getInputStream(String.format("%s(%s)", dataset, member));
        return FileUtil.getTextStreamData(inputStream);
    }

    private String getTextContent(final String dataset) throws ZosmfRequestException, IOException {
        LOG.debug("*** getTextContent member ***");
        final var inputStream = getInputStream(dataset);
        return FileUtil.getTextStreamData(inputStream);
    }

    private InputStream getBinaryContent(final String dataset, final String member) throws ZosmfRequestException {
        LOG.debug("*** getBinaryContent member ***");
        return getInputStream(String.format("%s(%s)", dataset, member));
    }

    public InputStream getInputStream(final String target) throws ZosmfRequestException {
        LOG.debug("*** getInputStream ***");
        if (dlParams == null) {
            dlParams = new DownloadParams.Builder().build();
        }
        return dsnGet.get(target, dlParams);
    }


}
