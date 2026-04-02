package zos.shell.controller.factory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.ConsoleController;
import zos.shell.controller.EnvVariableController;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.factory.AbstractDependencyControllerFactory;
import zos.shell.controller.factory.type.ConsoleControllerType;
import zos.shell.service.console.ConsoleService;
import zos.shell.service.env.EnvVariableService;
import zowe.client.sdk.core.ZosConnection;

public class ConsoleControllerFactory extends AbstractDependencyControllerFactory<ConsoleControllerType.Name> {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleControllerFactory.class);

    public ConsoleController getConsoleController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getConsoleController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                ConsoleControllerType.Name.CONSOLE,
                ConsoleController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var consoleService = new ConsoleService(connection, timeout);
                    return new ConsoleController(consoleService, envVariableController, dependency);
                }
        );
    }

}
