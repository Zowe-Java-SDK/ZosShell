package zos.shell.controller.factory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.*;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.factory.AbstractControllerFactory;
import zos.shell.controller.factory.AbstractDependencyControllerFactory;
import zos.shell.controller.factory.type.GlobalControllerType;
import zos.shell.service.echo.EchoService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.localfile.LocalFileService;
import zos.shell.service.search.SearchCacheService;
import zos.shell.service.uname.UnameService;
import zos.shell.service.usermod.UsermodService;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.methods.ConsoleCmd;

public class GlobalControllerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerFactory.class);

    private final EnvVariableController envVariableController = new EnvVariableController(new EnvVariableService());
    private final SimpleControllerCache simpleControllerCache = new SimpleControllerCache();
    private final DependencyControllerCache dependencyControllerCache = new DependencyControllerCache();

    public EchoController getEchoController() {
        LOG.debug("*** getEchoController ***");
        return this.simpleControllerCache.getOrCreateController(
                GlobalControllerType.Name.ECHO,
                EchoController.class,
                () -> new EchoController(new EchoService(this.envVariableController))
        );
    }

    public EnvVariableController getEnvVariableController() {
        LOG.debug("*** getEnvVariableController ***");
        return this.envVariableController;
    }

    public LocalFilesController getLocalFilesController() {
        LOG.debug("*** getLocalFilesController ***");
        return this.simpleControllerCache.getOrCreateController(
                GlobalControllerType.Name.LOCAL_FILE,
                LocalFilesController.class,
                () -> new LocalFilesController(new LocalFileService(this.envVariableController))
        );
    }

    public SearchCacheController getSearchCacheController() {
        LOG.debug("*** getSearchCacheController ***");
        return this.simpleControllerCache.getOrCreateController(
                GlobalControllerType.Name.SEARCH_CACHE,
                SearchCacheController.class,
                () -> new SearchCacheController(new SearchCacheService())
        );
    }

    public UnameController getUnameController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getUnameController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.dependencyControllerCache.getOrCreateController(
                GlobalControllerType.Name.UNAME,
                UnameController.class,
                dependency,
                () -> {
                    var unameService = new UnameService(new ConsoleCmd(connection), timeout);
                    return new UnameController(unameService, this.envVariableController, dependency);
                }
        );
    }

    public UsermodController getUsermodController(final ZosConnection connection, final int index) {
        LOG.debug("*** getUsermodController ***");
        return new UsermodController(new UsermodService(connection, Math.max(0, index - 1)));
    }

    private static final class SimpleControllerCache extends AbstractControllerFactory<GlobalControllerType.Name> {
    }

    private static final class DependencyControllerCache extends AbstractDependencyControllerFactory<GlobalControllerType.Name> {
    }

}
