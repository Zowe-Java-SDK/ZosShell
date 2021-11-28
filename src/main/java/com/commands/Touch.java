package com.commands;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosfiles.ZosDsn;

public class Touch {

    private final TextTerminal<?> terminal;
    private final ZosDsn zosDsn;

    public Touch(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.zosDsn = new ZosDsn(connection);
    }

    public void touch(String dataSet, String member) {
        if (!Util.isDataSet(dataSet)) {
            terminal.println(Constants.INVALID_DATASET);
            return;
        }
        if (!Util.isMember(member)) {
            terminal.println(Constants.INVALID_MEMBER);
            return;
        }

        try {
            zosDsn.writeDsn(dataSet, member, "");
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }

        terminal.println(member + " successfully created.");
    }

}
