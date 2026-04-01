package zos.shell.controller.container;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.*;
import zos.shell.controller.container.type.ContainerType;
import zos.shell.controller.dependency.Dependency;
import zos.shell.service.checksum.CheckSumService;
import zos.shell.service.console.ConsoleService;
import zos.shell.service.echo.EchoService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.job.browse.BrowseLogService;
import zos.shell.service.job.download.DownloadJobService;
import zos.shell.service.job.processlst.ProcessLstService;
import zos.shell.service.job.purge.PurgeService;
import zos.shell.service.job.submit.SubmitService;
import zos.shell.service.job.tail.TailService;
import zos.shell.service.job.terminate.TerminateService;
import zos.shell.service.localfile.LocalFileService;
import zos.shell.service.omvs.SshService;
import zos.shell.service.path.PathService;
import zos.shell.service.search.SearchCacheService;
import zos.shell.service.tso.TsoService;
import zos.shell.service.uname.UnameService;
import zos.shell.service.usermod.UsermodService;
import zos.shell.singleton.ConnSingleton;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.methods.ConsoleCmd;
import zowe.client.sdk.zosjobs.methods.JobDelete;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.methods.JobSubmit;
import zowe.client.sdk.zostso.methods.TsoCmd;

import java.util.HashMap;
import java.util.Map;

public class ControllerFactoryContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerFactoryContainer.class);
    private final CheckSumService checkSumService = new CheckSumService();
    private final EnvVariableController envVariableController = new EnvVariableController(new EnvVariableService());
    private final PathService pathService = new PathService(ConnSingleton.getInstance(), this.envVariableController);
    private final Map<ContainerType.Name, Object> controllers = new HashMap<>();

    ControllerFactoryContainer() {
        LOG.debug("*** ControllerFactoryContainer ***");
    }

    public BrowseJobController getBrowseJobController(final ZosConnection connection, final boolean isAll,
                                                      final long timeout) {
        LOG.debug("*** getBrowseJobController ***");
        var controller = (BrowseJobController) controllers.get(ContainerType.Name.BROWSE_JOB);
        var dependency = new Dependency.Builder().zosConnection(connection).toggle(isAll).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var jobGet = new JobGet(connection);
            var service = new BrowseLogService(jobGet, isAll, timeout);
            controller = new BrowseJobController(service, this.envVariableController, dependency);
            this.controllers.put(ContainerType.Name.BROWSE_JOB, controller);
        }
        return controller;
    }

    public CancelController getCancelController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCancelController ***");
        var controller = (CancelController) controllers.get(ContainerType.Name.CANCEL);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var issueConsole = new ConsoleCmd(connection);
            var service = new TerminateService(issueConsole, timeout);
            controller = new CancelController(service, this.envVariableController, dependency);
            this.controllers.put(ContainerType.Name.CANCEL, controller);
        }
        return controller;
    }

    public ConsoleController getConsoleController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getConsoleController ***");
        var controller = (ConsoleController) controllers.get(ContainerType.Name.CONSOLE);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var service = new ConsoleService(connection, timeout);
            controller = new ConsoleController(service, this.envVariableController, dependency);
            this.controllers.put(ContainerType.Name.CONSOLE, controller);
        }
        return controller;
    }

    public DownloadJobController getDownloadJobController(final ZosConnection connection, final boolean isAll,
                                                          final String jobId, final long timeout) {
        LOG.debug("*** getDownloadJobController ***");
        var controller = (DownloadJobController) controllers.get(ContainerType.Name.DOWNLOAD_JOB);
        var dependency = new Dependency.Builder().zosConnection(connection).toggle(isAll).data(jobId).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var jobGet = new JobGet(connection);
            var service = new DownloadJobService(jobGet, this.pathService, isAll, jobId, timeout);
            controller = new DownloadJobController(service, dependency);
            this.controllers.put(ContainerType.Name.DOWNLOAD_JOB, controller);
        }
        return controller;
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


    public ProcessLstController getProcessLstController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getProcessLstController ***");
        var controller = (ProcessLstController) controllers.get(ContainerType.Name.PROCESS_LIST);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var service = new ProcessLstService(new JobGet(connection), timeout);
            controller = new ProcessLstController(service, dependency);
            this.controllers.put(ContainerType.Name.PROCESS_LIST, controller);
        }
        return controller;
    }

    public PurgeController getPurgeController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getPurgeController ***");
        var controller = (PurgeController) controllers.get(ContainerType.Name.PURGE);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var jobDelete = new JobDelete(connection);
            var jobGet = new JobGet(connection);
            var service = new PurgeService(jobDelete, jobGet, timeout);
            controller = new PurgeController(service, dependency);
            this.controllers.put(ContainerType.Name.PURGE, controller);
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

    public StopController getStopController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getStopController ***");
        var controller = (StopController) controllers.get(ContainerType.Name.STOP);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var issueConsole = new ConsoleCmd(connection);
            var service = new TerminateService(issueConsole, timeout);
            controller = new StopController(service, this.envVariableController, dependency);
            this.controllers.put(ContainerType.Name.STOP, controller);
        }
        return controller;
    }

    public SubmitController getSubmitController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getSubmitController ***");
        var controller = (SubmitController) controllers.get(ContainerType.Name.SUBMIT);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var jobSubmit = new JobSubmit(connection);
            var service = new SubmitService(jobSubmit, timeout);
            controller = new SubmitController(service, dependency);
            this.controllers.put(ContainerType.Name.SUBMIT, controller);
        }
        return controller;
    }

    public TailController getTailController(final ZosConnection connection, final TextTerminal<?> terminal,
                                            final long timeout) {
        LOG.debug("*** getTailController ***");
        var controller = (TailController) controllers.get(ContainerType.Name.TAIL);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var jobGet = new JobGet(connection);
            var service = new TailService(terminal, jobGet, timeout);
            controller = new TailController(service, dependency);
            this.controllers.put(ContainerType.Name.TAIL, controller);
        }
        return controller;
    }

    public TsoController getTsoController(final ZosConnection connection,
                                          final String accountNumber,
                                          final long timeout) {
        LOG.debug("*** getTsoController ***");
        var controller = (TsoController) controllers.get(ContainerType.Name.TSO);
        var dependency = new Dependency.Builder().zosConnection(connection).timeout(timeout).data(accountNumber).build();
        if (controller == null || controller.isNotValid(dependency)) {
            var issueTso = new TsoCmd(connection, accountNumber);
            var service = new TsoService(issueTso, timeout);
            controller = new TsoController(service, dependency);
            this.controllers.put(ContainerType.Name.TSO, controller);
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
