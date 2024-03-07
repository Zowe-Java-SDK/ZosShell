package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.tso.TsoService;

public class TsoController {

    private static final Logger LOG = LoggerFactory.getLogger(TsoController.class);

    private final TsoService tsoService;
    private final EnvVariableController envVariableController;

    public TsoController(final TsoService tsoService, final EnvVariableController envVariableController) {
        LOG.debug("*** TsoController ***");
        this.tsoService = tsoService;
        this.envVariableController = envVariableController;
    }

    public String issueCommand(final String command) {
        LOG.debug("*** issueCommand ***");
        ResponseStatus responseStatus = tsoService.issueCommand(
                envVariableController.getValueByEnv("ACCOUNT_NUMBER"), command);
        return responseStatus.getMessage();
    }

}
