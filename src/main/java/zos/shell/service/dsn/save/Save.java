package zos.shell.service.dsn.save;

import com.google.common.base.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.DownloadCmd;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Save {

    private static final Logger LOG = LoggerFactory.getLogger(Save.class);

    private final DsnWrite dsnWrite;

    public Save(final DsnWrite dsnWrite) {
        LOG.debug("*** Save ***");
        this.dsnWrite = dsnWrite;
    }

    public ResponseStatus save(final String dataset, final String memberOrDataset) {
        LOG.debug("*** save ***");
        if (!Util.isDataSet(dataset)) {
            return new ResponseStatus(Constants.INVALID_DATASET, false);
        }

        var isSequentialDataSet = false;
        if (Util.isDataSet(memberOrDataset)) {
            isSequentialDataSet = true;
        } else if (!Util.isMember(memberOrDataset)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }

        String fileName;
        if (SystemUtils.IS_OS_WINDOWS) {
            fileName = DownloadCmd.DIRECTORY_PATH_WINDOWS + dataset + "\\" + memberOrDataset;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            fileName = DownloadCmd.DIRECTORY_PATH_MAC + dataset + "/" + memberOrDataset;
        } else {
            return new ResponseStatus(Constants.OS_ERROR, false);
        }

        final var arrowMsg = Strings.padStart(memberOrDataset,
                Constants.STRING_PAD_LENGTH, ' ') + Constants.ARROW;

        try (final var br = new BufferedReader(new FileReader(fileName))) {
            final var sb = new StringBuilder();
            var line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            final var content = sb.toString().replaceAll("(\\r)", "");

            if (isSequentialDataSet) {
                dsnWrite.write(memberOrDataset, content);
            } else {
                dsnWrite.write(dataset, memberOrDataset, content);
            }
        } catch (ZosmfRequestException e) {
            final var errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus(arrowMsg + (errMsg != null ? errMsg : e.getMessage()), false);
        } catch (IOException e) {
            return new ResponseStatus(arrowMsg + e.getMessage(), false);
        }

        return new ResponseStatus(arrowMsg + "saved", true);
    }

}
