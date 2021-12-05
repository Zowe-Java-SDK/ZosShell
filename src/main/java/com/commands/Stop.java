package com.commands;

import core.ZOSConnection;
import org.beryx.textio.TextTerminal;

public class Stop extends Terminate {

    public Stop(TextTerminal<?> terminal, ZOSConnection connection) {
        super(terminal, connection);
    }

    public void stop(String param) {
        this.kill(Type.STOP, param);
    }

}
