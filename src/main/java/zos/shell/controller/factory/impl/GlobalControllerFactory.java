package zos.shell.controller.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.*;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.factory.type.ContainerType;
import zos.shell.service.echo.EchoService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.localfile.LocalFileService;
import zos.shell.service.omvs.SshService;
import zos.shell.service.search.SearchCacheService;
import zos.shell.service.uname.UnameService;
import zos.shell.service.usermod.UsermodService;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.methods.ConsoleCmd;

import java.util.HashMap;
import java.util.Map;

public class ControllerFactoryContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerFactoryContainer.class);
    private final EnvVariableController envVariableController = new EnvVariableController(new EnvVariableService());
    private final Map<ContainerType.Name, Object> controllers = new HashMap<>();

    ControllerFactoryContainer() {
        LOG.debug("*** ControllerFactoryContainer ***");
    }

    public EchoController getEchoController() {
        LOG.debug("*** getEchoController ***");
        var controller = (EchoController) controllers.get(ContainerType.Name.ECHO);
        if (controller == null) {
            var service = new EchoService(this.envVariableController);
            controller = new EchoController(service);
            this.controllers.put(ContainerType.Name.ECHO, controller);
        }
        return controller;
    }

    public EnvVariableController getEnvVariableController() {
        LOG.debug("*** getEnvVariableController ***");
        return this.envVariableController;
    }

    public LocalFilesController getLocalFilesController() {
        LOG.debug("*** getLocalFilesController ***");
        var controller = (LocalFilesController) controllers.get(ContainerType.Name.LOCAL_FILE);
        if (controller == null) {
            var service = new LocalFileService(this.envVariableController);
            controller = new LocalFilesController(service);
            this.controllers.put(ContainerType.Name.LOCAL_FILE, controller);
        }
        return controller;
    }

    public SearchCacheController getSearchCacheController() {
        LOG.debug("*** getSearchCacheController ***");
        var controller = (SearchCacheController) controllers.get(ContainerType.Name.SEARCH_CACHE);
        if (controller == null) {
            var service = new SearchCacheService();
            controller = new SearchCacheController(service);
            this.controllers.put(ContainerType.Name.SEARCH_CACHE, controller);
        }
        return controller;
    }

    public UnameController getUnameController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getUnameController ***");
        var controller = (UnameController) controllers.get(ContainerType.Name.UNAME);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var issueConsole = new ConsoleCmd(connection);
            var service = new UnameService(issueConsole, timeout);
            controller = new UnameController(service, this.envVariableController, dependency);
            this.controllers.put(ContainerType.Name.UNAME, controller);
        }
        return controller;
    }

    public UsermodController getUsermodController(final ZosConnection connection, final int index) {
        LOG.debug("*** getUsermodController ***");
        var service = new UsermodService(connection, index == 0 ? index : index - 1);
        return new UsermodController(service);
    }

    public UssController getUssController(final SshConnection connection) {
        LOG.debug("*** getUssController ***");
        var controller = (UssController) controllers.get(ContainerType.Name.USS);
        var dependency = new Dependency.Builder().sshConnection(connection).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var service = new SshService(connection);
            controller = new UssController(service, dependency);
            this.controllers.put(ContainerType.Name.USS, controller);
        }
        return controller;
    }

}
