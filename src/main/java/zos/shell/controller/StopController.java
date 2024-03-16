package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.terminate.TerminateService;
import zos.shell.singleton.configuration.ConfigSingleton;

public class StopController {

    private static final Logger LOG = LoggerFactory.getLogger(StopController.class);

    private final TerminateService terminateService;
    private final ConfigSingleton configSingleton;
    private final EnvVariableController envVariableController;

    public StopController(final TerminateService terminateService, final ConfigSingleton configSingleton,
                          final EnvVariableController envVariableController) {
        LOG.debug("*** TerminateController ***");
        this.terminateService = terminateService;
        this.configSingleton = configSingleton;
        this.envVariableController = envVariableController;
    }

    public String stop(final String target) {
        LOG.debug("*** stop ***");
        String consoleName = envVariableController.getValueByEnv("CONSOLE_NAME").trim();
        if (consoleName.isBlank()) {
            consoleName = configSingleton.getConfigSettings().getConsoleName();
        }
        ResponseStatus responseStatus = terminateService.terminate(TerminateService.Type.STOP, consoleName, target);
        return responseStatus.getMessage();
    }

}
