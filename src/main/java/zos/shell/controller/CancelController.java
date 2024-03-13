package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.terminate.TerminateService;
import zos.shell.singleton.configuration.ConfigSingleton;

public class CancelController {

    private static final Logger LOG = LoggerFactory.getLogger(CancelController.class);

    private final TerminateService terminateService;
    private final ConfigSingleton configSingleton;
    private final EnvVariableController envVariableController;

    public CancelController(final TerminateService terminateService, final ConfigSingleton configSingleton,
                            final EnvVariableController envVariableController) {
        LOG.debug("*** CancelController ***");
        this.terminateService = terminateService;
        this.configSingleton = configSingleton;
        this.envVariableController = envVariableController;
    }

    public String cancel(final String target) {
        LOG.debug("*** cancel ***");
        String consoleName = envVariableController.getValueByEnv("CONSOLE_NAME").trim();
        if (consoleName.isBlank()) {
            consoleName = configSingleton.getConfigSettings().getConsoleName();
        }
        ResponseStatus responseStatus = terminateService.terminate(TerminateService.Type.CANCEL, consoleName, target);
        return responseStatus.getMessage();
    }

}
