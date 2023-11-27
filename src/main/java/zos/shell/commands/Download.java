package zos.shell.commands;

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.DirectorySetup;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.dsn.input.DownloadParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Download {

    private static final Logger LOG = LoggerFactory.getLogger(Download.class);

    public static final String DIRECTORY_PATH_WINDOWS = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\";
    public static final String DIRECTORY_PATH_MAC = Constants.PATH_FILE_DIRECTORY_MAC + "/";
    private final DsnGet dsnGet;
    private DownloadParams dlParams;
    private final boolean isBinary;

    public Download(DsnGet dsnGet, boolean isBinary) {
        LOG.debug("*** Download ***");
        this.dsnGet = dsnGet;
        this.isBinary = isBinary;
    }

    public ResponseStatus download(String dataSet, String member) {
        LOG.debug("*** download ***");
        if (!Util.isMember(member)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }
        var message = Strings.padStart(member, 8, ' ') + Constants.ARROW;

        final var dirSetup = new DirectorySetup();
        try {
            dirSetup.initialize(dataSet, member);
        } catch (Exception e) {
            return new ResponseStatus(message + Constants.OS_ERROR, false);
        }

        try {
            String textContent;
            InputStream binaryContent;

            if (!isBinary) {
                dlParams = new DownloadParams.Builder().build();
                textContent = getTextContent(dataSet, member);
                if (textContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                Util.writeTextFile(textContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
            } else {
                dlParams = new DownloadParams.Builder().binary(true).build();
                binaryContent = getBinaryContent(dataSet, member);
                if (binaryContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                writeBinaryFile(binaryContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
            }
            message += "downloaded to " + dirSetup.getFileNamePath();
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                return new ResponseStatus(message + Constants.CONNECTION_REFUSED, false);
            }
            return new ResponseStatus(message + e.getMessage(), false);
        }
        return new ResponseStatus(message, true, dirSetup.getFileNamePath());
    }

    private void writeBinaryFile(InputStream input, String directoryPath, String fileNamePath) throws IOException {
        LOG.debug("*** writeBinaryFile ***");
        Files.createDirectories(Paths.get(directoryPath));
        FileUtils.copyInputStreamToFile(input, new File(fileNamePath));
    }

    private String getTextContent(String dataSet, String member) throws Exception {
        LOG.debug("*** getTextContent member ***");
        final var inputStream = getInputStream(dataSet, member);
        return getTextStreamData(inputStream);
    }

    private InputStream getBinaryContent(String dataSet, String member) {
        LOG.debug("*** getBinaryContent member ***");
        return getInputStream(dataSet, member);
    }

    public InputStream getInputStream(String dataSet, String member) {
        LOG.debug("*** getInputStream member ***");
        if (dlParams == null) {
            dlParams = new DownloadParams.Builder().build();
        }
        return dsnGet.get(String.format("%s(%s)", dataSet, member), dlParams);
    }

    private String getTextStreamData(InputStream inputStream) throws IOException {
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