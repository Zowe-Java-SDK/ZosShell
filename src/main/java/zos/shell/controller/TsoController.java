package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.tso.TsoService;
import zos.shell.singleton.configuration.ConfigSingleton;

public class TsoController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(TsoController.class);

    private final TsoService tsoService;
    private final ConfigSingleton configSingleton;
    private final EnvVariableController envVariableController;

    public TsoController(final TsoService tsoService, final ConfigSingleton configSingleton,
                         final EnvVariableController envVariableController, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** TsoController ***");
        this.tsoService = tsoService;
        this.configSingleton = configSingleton;
        this.envVariableController = envVariableController;
    }

    public String issueCommand(final String command) {
        LOG.debug("*** issueCommand ***");
        String accountNumber = envVariableController.getValueByEnv("ACCOUNT_NUMBER").trim();
        if (accountNumber.isBlank()) {
            accountNumber = configSingleton.getConfigSettings().getAccountNumber();
        }
        ResponseStatus responseStatus = tsoService.issueCommand(accountNumber, command);
        return responseStatus.getMessage();
    }

}
