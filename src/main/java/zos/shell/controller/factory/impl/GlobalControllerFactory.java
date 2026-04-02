package zos.shell.controller.factory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.*;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.factory.type.GlobalControllerType;
import zos.shell.service.echo.EchoService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.localfile.LocalFileService;
import zos.shell.service.search.SearchCacheService;
import zos.shell.service.uname.UnameService;
import zos.shell.service.usermod.UsermodService;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.methods.ConsoleCmd;

import java.util.HashMap;
import java.util.Map;

public class GlobalControllerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerFactory.class);

    private final EnvVariableController envVariableController = new EnvVariableController(new EnvVariableService());
    private final Map<GlobalControllerType.Name, Object> controllers = new HashMap<>();

    public EchoController getEchoController() {
        LOG.debug("*** getEchoController ***");
        var controller = (EchoController) this.controllers.get(GlobalControllerType.Name.ECHO);
        if (controller == null) {
            controller = new EchoController(new EchoService(this.envVariableController));
            this.controllers.put(GlobalControllerType.Name.ECHO, controller);
        }
        return controller;
    }

    public EnvVariableController getEnvVariableController() {
        LOG.debug("*** getEnvVariableController ***");
        return this.envVariableController;
    }

    public LocalFilesController getLocalFilesController() {
        LOG.debug("*** getLocalFilesController ***");
        var controller = (LocalFilesController) this.controllers.get(GlobalControllerType.Name.LOCAL_FILE);
        if (controller == null) {
            controller = new LocalFilesController(new LocalFileService(this.envVariableController));
            this.controllers.put(GlobalControllerType.Name.LOCAL_FILE, controller);
        }
        return controller;
    }

    public SearchCacheController getSearchCacheController() {
        LOG.debug("*** getSearchCacheController ***");
        var controller = (SearchCacheController) this.controllers.get(GlobalControllerType.Name.SEARCH_CACHE);
        if (controller == null) {
            controller = new SearchCacheController(new SearchCacheService());
            this.controllers.put(GlobalControllerType.Name.SEARCH_CACHE, controller);
        }
        return controller;
    }

    public UnameController getUnameController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getUnameController ***");
        var controller = (UnameController) this.controllers.get(GlobalControllerType.Name.UNAME);
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        if (controller == null || controller.isNotValid(dependency)) {
            controller = new UnameController(
                    new UnameService(new ConsoleCmd(connection), timeout),
                    this.envVariableController,
                    dependency
            );
            this.controllers.put(GlobalControllerType.Name.UNAME, controller);
        }

        return controller;
    }

    public UsermodController getUsermodController(final ZosConnection connection, final int index) {
        LOG.debug("*** getUsermodController ***");
        return new UsermodController(
                new UsermodService(connection, index == 0 ? index : index - 1)
        );
    }
    
}
