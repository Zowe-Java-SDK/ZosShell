package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zowe.client.sdk.core.ZosConnection;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChangeConn {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeConn.class);

    private final TextTerminal<?> terminal;
    private final List<ZosConnection> connections;

    public ChangeConn(TextTerminal<?> terminal, List<ZosConnection> connections) {
        LOG.debug("*** ChangeConn ***");
        this.terminal = terminal;
        this.connections = connections;
    }

    public ZosConnection changeConnection(ZosConnection connection, String[] commands) {
        LOG.debug("*** changeConnection ***");
        var index = Integer.parseInt(commands[1]);
        if (index-- > connections.size()) {
            terminal.println(Constants.NO_CONNECTION);
            return connection;
        }
        final var newConnection = connections.get(index);
        terminal.println("Connected to " + newConnection.getHost() + " with user " + newConnection.getUser() + ".");
        return newConnection;
    }

    public void displayConnections(ZosConnection connection) {
        LOG.debug("*** displayConnections ***");
        if (connection != null) {
            var i = new AtomicInteger(1);
            connections.forEach(c ->
                    terminal.println(i.getAndIncrement() + " " + "hostname: " + c.getHost() + ", port: " +
                            c.getZosmfPort() + ", user: " + c.getUser())
            );
        } else {
            terminal.println(Constants.NO_CONNECTION_INFO);
        }
    }

}
