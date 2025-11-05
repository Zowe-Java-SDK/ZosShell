package zos.shell.service.omvs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.zosuss.exception.UssCmdException;
import zowe.client.sdk.zosuss.method.UssCmd;

import java.util.regex.Pattern;

public class SshService {

    private static final Logger LOG = LoggerFactory.getLogger(SshService.class);

    private final SshConnection sshConnection;

    public SshService(final SshConnection sshConnection) {
        LOG.debug("*** SshService ***");
        this.sshConnection = sshConnection;
    }

    public String sshCommand(String command) {
        LOG.debug("*** sshCommand ***");
        var p = Pattern.compile("\"([^\"]*)\"");
        var m = p.matcher(command);

        while (m.find()) {
            command = m.group(1);
        }

        try {
            var issueUss = new UssCmd(sshConnection);
            // 10,000 is the timeout value in milliseconds
            return issueUss.issueCommand(command, 10000);
        } catch (UssCmdException e) {
            LOG.debug(String.valueOf(e));
            return e.getMessage();
        }
    }

}
