package com.commands;

import com.Constants;
import com.google.common.base.Strings;
import com.utility.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
import utility.UtilIO;
import zosfiles.ZosDsnDownload;
import zosfiles.input.DownloadParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Download {

    private final TextTerminal<?> terminal;
    private final ZosDsnDownload download;
    private final DownloadParams dlParams = new DownloadParams.Builder().build();
    public static final String DIRECTORY_PATH = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\";

    public Download(TextTerminal<?> terminal, ZosDsnDownload download) {
        this.terminal = terminal;
        this.download = download;
    }

    public DownloadStatus download(String dataSet, String member) {
        var message = Strings.padStart(member, 8, ' ') + Constants.ARROW;
        try {
            var content = getContent(dataSet, member);
            if (content == null) {
                return new DownloadStatus(message + Constants.DOWNLOAD_FAIL, false);
            }
            if (!SystemUtils.IS_OS_WINDOWS) {
                return new DownloadStatus(message + Constants.WINDOWS_ERROR_MSG, false);
            }
            var directoryPath = DIRECTORY_PATH + dataSet;
            var fileNamePath = directoryPath + "\\" + member;
            message = writeFile(message, content, directoryPath, fileNamePath);
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED))
                return new DownloadStatus(message + Constants.CONNECTION_REFUSED, false);
            return new DownloadStatus(message + e.getMessage(), false);
        }
        return new DownloadStatus(message, true);
    }

    public InputStream getInputStream(String dataSet, String param) throws Exception {
        InputStream inputStream;
        if (Util.isDataSet(param)) {
            inputStream = download.downloadDsn(String.format("%s", param), dlParams);
        } else {
            inputStream = download.downloadDsn(String.format("%s(%s)", dataSet, param), dlParams);
        }
        return inputStream;
    }

    protected String getContent(String dataSet, String param) throws Exception {
        var inputStream = getInputStream(dataSet, param);
        return getStreamData(inputStream);
    }

    protected String getStreamData(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, UtilIO.UTF8);
            var content = writer.toString();
            inputStream.close();
            return content;
        }
        return null;
    }

    protected String writeFile(String message, String content, String directoryPath, String fileNamePath)
            throws IOException {
        Files.createDirectories(Paths.get(directoryPath));
        Files.write(Paths.get(fileNamePath), content.getBytes());
        message += "downloaded to " + fileNamePath;
        return message;
    }

}
