package zos.shell.commands;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.ZosDsn;

import java.io.BufferedReader;
import java.io.FileReader;

public class Save {

    private static Logger LOG = LoggerFactory.getLogger(Save.class);

    private final ZosDsn zosDsn;

    public Save(ZosDsn zosDsn) {
        LOG.debug("*** Save ***");
        this.zosDsn = zosDsn;
    }

    public ResponseStatus save(String dataSet, String member) {
        LOG.debug("*** save ***");
        if (!Util.isDataSet(dataSet)) {
            return new ResponseStatus(Constants.INVALID_DATASET, false);
        }

        var isSequentialDataSet = false;
        if (Util.isDataSet(member)) {
            isSequentialDataSet = true;
        } else if (!Util.isMember(member)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }

        String fileName;
        if (SystemUtils.IS_OS_WINDOWS) {
            fileName = Download.DIRECTORY_PATH_WINDOWS + dataSet + "\\" + member;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            fileName = Download.DIRECTORY_PATH_MAC + dataSet + "/" + member;
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
                zosDsn.writeDsn(member, content);
            } else {
                zosDsn.writeDsn(dataSet, member, content);
            }
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }

        return new ResponseStatus(member + " successfully saved...", true);
    }

}

