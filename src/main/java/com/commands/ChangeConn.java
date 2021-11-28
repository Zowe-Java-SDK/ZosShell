package com.commands;

import com.Constants;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChangeConn {

    private final TextTerminal<?> terminal;
    private final List<ZOSConnection> connections;

    public ChangeConn(TextTerminal<?> terminal, List<ZOSConnection> connections) {
        this.terminal = terminal;
        this.connections = connections;
    }

    public ZOSConnection changeConnection(ZOSConnection connection, String[] commands) {
        var index = Integer.parseInt(commands[1]);
        if (index-- > connections.size()) {
            terminal.println(Constants.NO_CONNECTION);
            return connection;
        }
        terminal.println("Connection changed successfully.");
        return connections.get(index);
    }

    public void displayConnections(ZOSConnection connection) {
        if (connection != null) {
            var i = new AtomicInteger(1);
            connections.forEach(c ->
                    terminal.println(i.getAndIncrement() + " " + "hostname: " + c.getHost() + ", port: " +
                            c.getZosmfPort() + ", user = " + c.getUser())
            );
        } else {
            terminal.println(Constants.NO_CONNECTION_INFO);
        }
    }

}
