package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.terminate.TerminateService;

public class CancelController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(CancelController.class);

    private final TerminateService terminateService;
    private final EnvVariableController envVariableController;

    public CancelController(final TerminateService terminateService,
                            final EnvVariableController envVariableController,
                            final Dependency dependency) {
        super(dependency);
        LOG.debug("*** CancelController ***");
        this.terminateService = terminateService;
        this.envVariableController = envVariableController;
    }

    public String cancel(final String target) {
        LOG.debug("*** cancel ***");
        String consoleName = envVariableController.getValueByEnv("CONSOLE_NAME");
        ResponseStatus responseStatus = terminateService.terminate(TerminateService.Type.CANCEL, consoleName, target);
        return responseStatus.getMessage();
    }

}
