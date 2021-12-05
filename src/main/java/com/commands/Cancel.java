package com.commands;

import core.ZOSConnection;
import org.beryx.textio.TextTerminal;

public class Cancel extends Terminate {

    public Cancel(TextTerminal<?> terminal, ZOSConnection connection) {
        super(terminal, connection);
    }

    public void cancel(String param) {
        this.kill(Type.CANCEL, param);
    }

}
