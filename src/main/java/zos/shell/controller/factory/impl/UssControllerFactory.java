package zos.shell.controller.factory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.UssController;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.factory.AbstractDependencyControllerFactory;
import zos.shell.controller.factory.type.UssControllerType;
import zos.shell.service.omvs.SshService;
import zowe.client.sdk.core.SshConnection;

public class UssControllerFactory extends AbstractDependencyControllerFactory<UssControllerType.Name> {

    private static final Logger LOG = LoggerFactory.getLogger(UssControllerFactory.class);

    public UssController getUssController(final SshConnection connection) {
        LOG.debug("*** getUssController ***");
        var dependency = new Dependency.Builder()
                .sshConnection(connection)
                .build();

        return this.getOrCreateController(
                UssControllerType.Name.USS,
                UssController.class,
                dependency,
                () -> {
                    var sshService = new SshService(connection);
                    return new UssController(sshService, dependency);
                }
        );
    }

}
