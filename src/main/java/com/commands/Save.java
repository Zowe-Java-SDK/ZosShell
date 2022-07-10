package com.commands;

import com.Constants;
import com.dto.ResponseStatus;
import com.utility.Util;
import org.apache.commons.lang3.SystemUtils;
import zowe.client.sdk.zosfiles.ZosDsn;

import java.io.BufferedReader;
import java.io.FileReader;

public class Save {

    private final ZosDsn zosDsn;

    public Save(ZosDsn zosDsn) {
        this.zosDsn = zosDsn;
    }

    public ResponseStatus save(String dataSet, String member) {
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
            var sb = new StringBuilder();
            var line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            var content = sb.toString().replaceAll("(\\r)", "");

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

