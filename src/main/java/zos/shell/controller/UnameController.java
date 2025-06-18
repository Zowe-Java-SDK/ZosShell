package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.service.uname.UnameService;
import zowe.client.sdk.core.ZosConnection;

public class UnameController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(UnameController.class);

    private final UnameService unameService;
    private final EnvVariableController envVariableController;

    public UnameController(final UnameService unameService,
                           final EnvVariableController envVariableController,
                           final Dependency dependency) {
        super(dependency);
        LOG.debug("*** UnameController ***");
        this.unameService = unameService;
        this.envVariableController = envVariableController;
    }

    public String uname(final ZosConnection connection) {
        LOG.debug("*** uname ***");
        String consoleName = envVariableController.getValueByEnv("CONSOLE_NAME");
        return unameService.getUname(connection.getHost(), consoleName);
    }

}
