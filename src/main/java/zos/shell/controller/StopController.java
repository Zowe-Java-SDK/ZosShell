package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.terminate.TerminateService;

public class StopController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(StopController.class);

    private final TerminateService terminateService;
    private final EnvVariableController envVariableController;

    public StopController(final TerminateService terminateService,
                          final EnvVariableController envVariableController,
                          final Dependency dependency) {
        super(dependency);
        LOG.debug("*** TerminateController ***");
        this.terminateService = terminateService;
        this.envVariableController = envVariableController;
    }

    public String stop(final String target) {
        LOG.debug("*** stop ***");
        String consoleName = envVariableController.getValueByEnv("CONSOLE_NAME");
        ResponseStatus responseStatus = terminateService.terminate(TerminateService.Type.STOP, consoleName, target);
        return responseStatus.getMessage();
    }

}
