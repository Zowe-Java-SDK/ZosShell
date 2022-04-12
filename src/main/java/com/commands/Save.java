package com.commands;

import com.Constants;
import com.utility.Util;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosfiles.ZosDsn;

import java.io.BufferedReader;
import java.io.FileReader;

public class Save {

    private final TextTerminal<?> terminal;
    private final ZosDsn zosDsn;

    public Save(TextTerminal<?> terminal, ZosDsn zosDsn) {
        this.terminal = terminal;
        this.zosDsn = zosDsn;
    }

    public void save(String dataSet, String member) {
        if (!Util.isDataSet(dataSet)) {
            terminal.println(Constants.INVALID_DATASET);
            return;
        }

        var isSequentialDataSet = false;
        if (Util.isDataSet(member)) {
            isSequentialDataSet = true;
        } else if (!Util.isMember(member)) {
            terminal.println(Constants.INVALID_MEMBER);
            return;
        }

        String fileName;
        if (SystemUtils.IS_OS_WINDOWS) {
            fileName = Download.DIRECTORY_PATH_WINDOWS + dataSet + "\\" + member;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            fileName = Download.DIRECTORY_PATH_MAC + dataSet + "/" + member;
        } else {
            terminal.println(Constants.OS_ERROR);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
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
            Util.printError(terminal, e.getMessage());
            return;
        }

        terminal.println(member + " successfully saved...");
    }

}

