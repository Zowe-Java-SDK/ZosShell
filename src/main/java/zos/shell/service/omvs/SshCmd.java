package zos.shell.service.omvs;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosuss.exception.IssueUssException;
import zowe.client.sdk.zosuss.method.IssueUss;

import java.util.Map;
import java.util.regex.Pattern;

public class SshCmd {

    private static final Logger LOG = LoggerFactory.getLogger(SshCmd.class);

    private final TextTerminal<?> terminal;
    private final SshConnection sshConnection;

    public SshCmd(TextTerminal<?> terminal, ZosConnection connection, Map<String, SshConnection> sshConnection) {
        LOG.debug("*** Ussh ***");
        this.terminal = terminal;
        this.sshConnection = sshConnection.get(connection.getHost());
    }

    public void sshCommand(String command) {
        LOG.debug("*** sshCommand ***");
        final var p = Pattern.compile("\"([^\"]*)\"");
        final var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }
        try {
            final var issueUss = new IssueUss(sshConnection);
            // 10000 is the timeout value in milliseconds
            terminal.println(issueUss.issueCommand(command, 10000));
        } catch (IssueUssException e) {
            terminal.println(e.getMessage());
        }
    }

}