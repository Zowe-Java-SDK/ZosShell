package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.core.SSHConnection;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosuss.Shell;

import java.util.Map;
import java.util.regex.Pattern;

public class Ussh {

    private static Logger LOG = LoggerFactory.getLogger(Ussh.class);

    private final TextTerminal<?> terminal;
    private final SSHConnection sshConnection;

    public Ussh(TextTerminal<?> terminal, ZOSConnection connection, Map<String, SSHConnection> sshConnections) {
        LOG.debug("*** Ussh ***");
        this.terminal = terminal;
        this.sshConnection = sshConnections.get(connection.getHost());
    }

    public void sshCommand(String command) {
        LOG.debug("*** sshCommand ***");
        final var p = Pattern.compile("\"([^\"]*)\"");
        final var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }
        try {
            final var shell = new Shell(sshConnection);
            // 10000 is the timeout value in milliseconds
            terminal.println(shell.executeSshCmd(command, 10000));
        } catch (Exception e) {
            terminal.println(e + "");
        }
    }

}
