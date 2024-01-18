package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.omvs.SshService;

public class UssController {

    private static final Logger LOG = LoggerFactory.getLogger(UssController.class);

    private final SshService sshService;

    public UssController(final SshService sshService) {
        LOG.debug("*** UssController ***");
        this.sshService = sshService;
    }

    public String issueUnixCommand(final String command) {
        LOG.debug("*** issueUnixCommand ***");
        return sshService.sshCommand(command);
    }

}
