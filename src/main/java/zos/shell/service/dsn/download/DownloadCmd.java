package zos.shell.service.dsn.download;

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.DirectorySetup;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.DownloadParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadCmd {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadCmd.class);

    public static final String DIRECTORY_PATH_WINDOWS = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\";
    public static final String DIRECTORY_PATH_MAC = Constants.PATH_FILE_DIRECTORY_MAC + "/";
    private final DsnGet dsnGet;
    private DownloadParams dlParams;
    private final boolean isBinary;

    public DownloadCmd(final DsnGet dsnGet, boolean isBinary) {
        LOG.debug("*** DownloadCmd ***");
        this.dsnGet = dsnGet;
        this.isBinary = isBinary;
    }

    public ResponseStatus member(final String dataset, final String member) {
        LOG.debug("*** member ***");
        if (!Util.isMember(member)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }
        var message = Strings.padStart(member, 8, ' ') + Constants.ARROW;

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
                Util.writeTextFile(textContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
            } else {
                dlParams = new DownloadParams.Builder().binary(true).build();
                binaryContent = getBinaryContent(dataset, member);
                if (binaryContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                writeBinaryFile(binaryContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
            }
            message += "downloaded to " + dirSetup.getFileNamePath();
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus(message + (errMsg != null ? errMsg : e.getMessage()), false);
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
            Util.writeTextFile(textContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());

        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus(message + (errMsg != null ? errMsg : e.getMessage()), false);
        } catch (IOException e) {
            return new ResponseStatus(message + e.getMessage(), false);
        }

        message += "downloaded to " + dirSetup.getFileNamePath();
        return new ResponseStatus(message, true, dirSetup.getFileNamePath());
    }

    private void writeBinaryFile(final InputStream input, final String directoryPath, final String fileNamePath)
            throws IOException {
        LOG.debug("*** writeBinaryFile ***");
        Files.createDirectories(Paths.get(directoryPath));
        FileUtils.copyInputStreamToFile(input, new File(fileNamePath));
    }

    private String getTextContent(final String dataset, final String member)
            throws ZosmfRequestException, IOException {
        LOG.debug("*** getTextContent member ***");
        final var inputStream = getInputStream(String.format("%s(%s)", dataset, member));
        return getTextStreamData(inputStream);
    }

    private String getTextContent(final String dataset) throws ZosmfRequestException, IOException {
        LOG.debug("*** getTextContent member ***");
        final var inputStream = getInputStream(dataset);
        return getTextStreamData(inputStream);
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

    private String getTextStreamData(final InputStream inputStream) throws IOException {
        LOG.debug("*** getTextStreamData ***");
        if (inputStream != null) {
            final var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, Constants.UTF8);
            final var content = writer.toString();
            inputStream.close();
            return content;
        }
        return null;
    }

}