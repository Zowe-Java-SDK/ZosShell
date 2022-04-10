package com.commands;

import com.Constants;
import com.dto.ResponseStatus;
import com.google.common.base.Strings;
import com.utility.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import zowe.client.sdk.utility.UtilIO;
import zowe.client.sdk.zosfiles.ZosDsnDownload;
import zowe.client.sdk.zosfiles.input.DownloadParams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Download {

    public static final String DIRECTORY_PATH = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\";
    private final ZosDsnDownload download;
    private DownloadParams dlParams;
    private final boolean isBinary;

    public Download(ZosDsnDownload download, boolean isBinary) {
        this.download = download;
        this.isBinary = isBinary;
    }

    public ResponseStatus download(String dataSet, String member) {
        var message = Strings.padStart(member, 8, ' ') + Constants.ARROW;

        if (!SystemUtils.IS_OS_WINDOWS) {
            return new ResponseStatus(message + Constants.WINDOWS_ERROR, false);
        }

        var directoryPath = DIRECTORY_PATH + dataSet;
        var fileNamePath = directoryPath + "\\" + member;

        try {
            String textContent;
            InputStream binaryContent;

            if (!isBinary) {
                dlParams = new DownloadParams.Builder().build();
                textContent = getTextContent(dataSet, member);
                if (textContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                writeTextFile(textContent, directoryPath, fileNamePath);
            } else {
                dlParams = new DownloadParams.Builder().binary(true).build();
                binaryContent = getBinaryContent(dataSet, member);
                if (binaryContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                writeBinaryFile(binaryContent, directoryPath, fileNamePath);
            }
            message += "downloaded to " + fileNamePath;
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                return new ResponseStatus(message + Constants.CONNECTION_REFUSED, false);
            }
            return new ResponseStatus(message + e.getMessage(), false);
        }
        return new ResponseStatus(message, true);
    }

    private void writeBinaryFile(InputStream input, String directoryPath, String fileNamePath) throws IOException {
        Files.createDirectories(Paths.get(directoryPath));
        FileUtils.copyInputStreamToFile(input, new File(fileNamePath));
    }

    private String getTextContent(String dataSet, String param) throws Exception {
        var inputStream = getInputStream(dataSet, param);
        return getTextStreamData(inputStream);
    }

    private InputStream getBinaryContent(String dataSet, String param) throws Exception {
        return getInputStream(dataSet, param);
    }

    public InputStream getInputStream(String dataSet, String param) throws Exception {
        InputStream inputStream;
        if (dlParams == null) {
            dlParams = new DownloadParams.Builder().build();
        }
        if (Util.isDataSet(param)) {
            inputStream = download.downloadDsn(String.format("%s", param), dlParams);
        } else {
            inputStream = download.downloadDsn(String.format("%s(%s)", dataSet, param), dlParams);
        }
        return inputStream;
    }

    private String getTextStreamData(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, UtilIO.UTF8);
            var content = writer.toString();
            inputStream.close();
            return content;
        }
        return null;
    }

    protected void writeTextFile(String content, String directoryPath, String fileNamePath) throws IOException {
        Files.createDirectories(Paths.get(directoryPath));
        Files.write(Paths.get(fileNamePath), content.getBytes());
    }

}
