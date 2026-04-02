package zos.shell.controller.factory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.TsoController;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.factory.AbstractDependencyControllerFactory;
import zos.shell.controller.factory.type.TsoControllerType;
import zos.shell.service.tso.TsoService;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zostso.methods.TsoCmd;

public class TsoControllerFactory extends AbstractDependencyControllerFactory<TsoControllerType.Name> {

    private static final Logger LOG = LoggerFactory.getLogger(TsoControllerFactory.class);

    public TsoController getTsoController(final ZosConnection connection,
                                          final String accountNumber,
                                          final long timeout) {
        LOG.debug("*** getTsoController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .data(accountNumber)
                .build();

        return this.getOrCreateController(
                TsoControllerType.Name.TSO,
                TsoController.class,
                dependency,
                () -> {
                    var issueTso = new TsoCmd(connection, accountNumber);
                    var tsoService = new TsoService(issueTso, timeout);
                    return new TsoController(tsoService, dependency);
                }
        );
    }

}
