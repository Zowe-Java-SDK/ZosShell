package zos.shell.service.dsn.save;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.path.PathService;
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
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC_OSX) {
            return new ResponseStatus(Constants.OS_ERROR, false);
        }

        final var isSequentialDataset = DsnUtil.isDataSet(target);
        PathService pathService;
        if (isSequentialDataset) {
            pathService = new PathService(target);
        } else {
            pathService = new PathService(dataset, target);
        }

        try (final var br = new BufferedReader(new FileReader(pathService.getPathWithFile()))) {
            final var sb = new StringBuilder();
            var line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            final var content = sb.toString().replaceAll("(\\r)", "");

            if (isSequentialDataset) {
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
