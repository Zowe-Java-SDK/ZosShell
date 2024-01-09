package zos.shell.service.dsn.save;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.DownloadDsnCmd;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
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

    public ResponseStatus save(final String dataset, final String target) {
        LOG.debug("*** save ***");
        final var isSequentialDataSet = DsnUtil.isDataSet(target);

        String fileName;
        if (SystemUtils.IS_OS_WINDOWS) {
            fileName = isSequentialDataSet ?
                    DownloadDsnCmd.DIRECTORY_PATH_WINDOWS + Constants.SEQUENTIAL_DIRECTORY_LOCATION + "\\" + target :
                    DownloadDsnCmd.DIRECTORY_PATH_WINDOWS + dataset + "\\" + target;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            fileName = isSequentialDataSet ?
                    DownloadDsnCmd.DIRECTORY_PATH_MAC + Constants.SEQUENTIAL_DIRECTORY_LOCATION + "/" + target :
                    DownloadDsnCmd.DIRECTORY_PATH_MAC + dataset + "/" + target;
        } else {
            return new ResponseStatus(Constants.OS_ERROR, false);
        }

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
                dsnWrite.write(target, content);
            } else {
                dsnWrite.write(dataset, target, content);
            }
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        } catch (IOException e) {
            return new ResponseStatus(e.getMessage(), false);
        }

        return new ResponseStatus(target + " saved", true);
    }

}
