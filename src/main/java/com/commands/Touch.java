package com.commands;

import com.Constants;
import com.dto.Member;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosfiles.ZosDsn;

public class Touch {

    private final TextTerminal<?> terminal;
    private final ZosDsn zosDsn;
    private final Member members;

    public Touch(TextTerminal<?> terminal, ZosDsn zosDsn, Member members) {
        this.terminal = terminal;
        this.zosDsn = zosDsn;
        this.members = members;
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

        // if member already exist skip write, touch will only create a new member
        var foundExistingMember = false;
        try {
            foundExistingMember = members.getMembers(dataSet).stream().anyMatch(m -> m.equalsIgnoreCase(member));
        } catch (Exception ignored) {
        }

        try {
            if (!foundExistingMember) {
                zosDsn.writeDsn(dataSet, member, "");
            } else {
                terminal.println(member + " already exists.");
                return;
            }
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }

        terminal.println(member + " successfully created.");
    }

}
