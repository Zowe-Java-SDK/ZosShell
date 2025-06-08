package zos.shell.controller.container;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.*;
import zos.shell.service.change.ChangeConnService;
import zos.shell.service.change.ChangeDirService;
import zos.shell.service.change.ChangeWinService;
import zos.shell.service.checksum.CheckSumService;
import zos.shell.service.console.ConsoleService;
import zos.shell.service.dsn.concat.ConcatService;
import zos.shell.service.dsn.copy.CopyService;
import zos.shell.service.dsn.count.CountService;
import zos.shell.service.dsn.delete.DeleteService;
import zos.shell.service.dsn.download.*;
import zos.shell.service.dsn.edit.EditService;
import zos.shell.service.dsn.list.ListingService;
import zos.shell.service.dsn.makedir.MakeDirService;
import zos.shell.service.dsn.save.SaveService;
import zos.shell.service.dsn.touch.TouchService;
import zos.shell.service.echo.EchoService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.grep.GrepService;
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
import zos.shell.service.rename.RenameService;
import zos.shell.service.search.SearchCacheService;
import zos.shell.service.tso.TsoService;
import zos.shell.service.uname.UnameService;
import zos.shell.service.usermod.UsermodService;
import zos.shell.singleton.ConnSingleton;
import zos.shell.singleton.configuration.ConfigSingleton;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;
import zowe.client.sdk.zosjobs.methods.JobDelete;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.methods.JobSubmit;
import zowe.client.sdk.zostso.method.IssueTso;

import java.util.HashMap;
import java.util.Map;

public class ControllerFactoryContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerFactoryContainer.class);
    private final EnvVariableController envVariableController = new EnvVariableController(new EnvVariableService());
    private final PathService pathService =
            new PathService(ConfigSingleton.getInstance(), ConnSingleton.getInstance(), this.envVariableController);
    private final Map<ContainerType.Name, Object> controllers = new HashMap<>();
    private final Map<ContainerType.Name, Dependency> dependencies = new HashMap<>();

    public ControllerFactoryContainer() {
        LOG.debug("*** ControllerFactoryContainer ***");
    }

    public BrowseJobController getBrowseJobController(final ZosConnection connection, final boolean isAll,
                                                      final long timeout) {
        LOG.debug("*** getBrowseJobController ***");
        var controller = (BrowseJobController) controllers.get(ContainerType.Name.BROWSE_JOB);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.BROWSE_JOB);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, isAll, timeout)) {
            var jobGet = new JobGet(connection);
            var service = new BrowseLogService(jobGet, isAll, timeout);
            controller = new BrowseJobController(service, this.envVariableController);
            dependencyCacheContainer = new Dependency(connection, isAll, timeout);
            this.controllers.put(ContainerType.Name.BROWSE_JOB, controller);
            this.dependencies.put(ContainerType.Name.BROWSE_JOB, dependencyCacheContainer);
        }
        return controller;
    }

    public CancelController getCancelController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCancelController ***");
        var controller = (CancelController) controllers.get(ContainerType.Name.CANCEL);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.CANCEL);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, false, timeout)) {
            var issueConsole = new IssueConsole(connection);
            var service = new TerminateService(issueConsole, timeout);
            controller = new CancelController(service, ConfigSingleton.getInstance(), this.envVariableController);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.CANCEL, controller);
            this.dependencies.put(ContainerType.Name.CANCEL, dependencyCacheContainer);
        }
        return controller;
    }

    public ChangeConnController getChangeConnController(final TextTerminal<?> terminal) {
        LOG.debug("*** getChangeConnController ***");
        var controller = (ChangeConnController) controllers.get(ContainerType.Name.CHANGE_CONNECTION);
        if (controller == null) {
            var service = new ChangeConnService(terminal);
            controller = new ChangeConnController(service);
            this.controllers.put(ContainerType.Name.CHANGE_CONNECTION, controller);
        }
        return controller;
    }

    public ChangeDirController getChangeDirController(final ZosConnection connection) {
        LOG.debug("*** getChangeDirController ***");
        var controller = (ChangeDirController) controllers.get(ContainerType.Name.CHANGE_DIRECTORY);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.CHANGE_DIRECTORY);
        if (controller == null || dependencyCacheContainer != null &&
                !dependencyCacheContainer.isZosConnectionSame(connection)) {
            var service = new ChangeDirService();
            controller = new ChangeDirController(service);
            dependencyCacheContainer = new Dependency(connection);
            this.controllers.put(ContainerType.Name.CHANGE_DIRECTORY, controller);
            this.dependencies.put(ContainerType.Name.CHANGE_DIRECTORY, dependencyCacheContainer);
        }
        return controller;
    }

    public ChangeWinController getChangeWinController(final TextTerminal<?> terminal) {
        LOG.debug("*** getChangeWinController ***");
        var controller = (ChangeWinController) controllers.get(ContainerType.Name.CHANGE_WINDOW);
        if (controller == null) {
            var service = new ChangeWinService(terminal);
            controller = new ChangeWinController(service);
            this.controllers.put(ContainerType.Name.CHANGE_DIRECTORY, controller);
        }
        return controller;
    }

    public ConcatController getConcatController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getConcatController ***");
        var controller = (ConcatController) controllers.get(ContainerType.Name.CONCAT);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.CONCAT);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var dsnGet = new DsnGet(connection);
            var download = new Download(dsnGet, this.pathService, false);
            var service = new ConcatService(download, timeout);
            controller = new ConcatController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.CONCAT, controller);
            this.dependencies.put(ContainerType.Name.CONCAT, dependencyCacheContainer);
        }
        return controller;
    }

    public ConsoleController getConsoleController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getConsoleController ***");
        var controller = (ConsoleController) controllers.get(ContainerType.Name.CONSOLE);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.CONSOLE);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var service = new ConsoleService(connection, timeout);
            controller = new ConsoleController(service, ConfigSingleton.getInstance(), this.envVariableController);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.CONSOLE, controller);
            this.dependencies.put(ContainerType.Name.CONSOLE, dependencyCacheContainer);
        }
        return controller;
    }

    public CopyController getCopyController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCopyController ***");
        var controller = (CopyController) controllers.get(ContainerType.Name.COPY);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.COPY);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var service = new CopyService(connection, timeout);
            controller = new CopyController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.COPY, controller);
            this.dependencies.put(ContainerType.Name.COPY, dependencyCacheContainer);
        }
        return controller;
    }

    public CountController getCountController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCountController ***");
        var controller = (CountController) controllers.get(ContainerType.Name.COUNT);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.COUNT);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var dsnList = new DsnList(connection);
            var service = new CountService(dsnList, timeout);
            controller = new CountController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.COUNT, controller);
            this.dependencies.put(ContainerType.Name.COUNT, dependencyCacheContainer);
        }
        return controller;
    }

    public DeleteController getDeleteController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getDeleteController ***");
        var controller = (DeleteController) controllers.get(ContainerType.Name.DELETE);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.DELETE);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var service = new DeleteService(connection, timeout);
            controller = new DeleteController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.DELETE, controller);
            this.dependencies.put(ContainerType.Name.DELETE, dependencyCacheContainer);
        }
        return controller;
    }

    public DownloadDsnController getDownloadDsnController(final ZosConnection connection, final boolean isBinary,
                                                          final long timeout) {
        LOG.debug("*** getDownloadDsnController ***");
        var controller = (DownloadDsnController) controllers.get(ContainerType.Name.DELETE);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.DELETE);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, isBinary, timeout)) {
            var downloadMemberService = new DownloadMemberService(connection, this.pathService, isBinary, timeout);
            var downloadPdsMemberService = new DownloadPdsMemberService(connection, this.pathService, isBinary, timeout);
            var downloadSeqDatasetService = new DownloadSeqDatasetService(connection, this.pathService, isBinary, timeout);
            var downloadAllMembersService = new DownloadAllMembersService(connection,
                    new DownloadMemberListService(connection, isBinary, timeout), timeout);
            var downloadMembersService = new DownloadMembersService(connection,
                    new DownloadMemberListService(connection, isBinary, timeout), timeout);
            controller = new DownloadDsnController(downloadMemberService, downloadPdsMemberService,
                    downloadSeqDatasetService, downloadAllMembersService, downloadMembersService);
            dependencyCacheContainer = new Dependency(connection, isBinary, timeout);
            this.controllers.put(ContainerType.Name.DELETE, controller);
            this.dependencies.put(ContainerType.Name.DELETE, dependencyCacheContainer);
        }
        return controller;
    }

    public DownloadJobController getDownloadJobController(final ZosConnection connection, final boolean isAll,
                                                          final long timeout) {
        LOG.debug("*** getDownloadJobController ***");
        var controller = (DownloadJobController) controllers.get(ContainerType.Name.DOWNLOAD_JOB);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.DOWNLOAD_JOB);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, isAll, timeout)) {
            var jobGet = new JobGet(connection);
            var service = new DownloadJobService(jobGet, this.pathService, isAll, timeout);
            controller = new DownloadJobController(service);
            dependencyCacheContainer = new Dependency(connection, isAll, timeout);
            this.controllers.put(ContainerType.Name.DOWNLOAD_JOB, controller);
            this.dependencies.put(ContainerType.Name.DOWNLOAD_JOB, dependencyCacheContainer);
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

    public EditController getEditController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getEditController ***");
        var controller = (EditController) controllers.get(ContainerType.Name.EDIT);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.EDIT);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var dsnGet = new DsnGet(connection);
            var download = new Download(dsnGet, this.pathService, false);
            var checkSumService = new CheckSumService();
            var editService = new EditService(download, this.pathService, checkSumService, timeout);
            controller = new EditController(editService);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.EDIT, controller);
            this.dependencies.put(ContainerType.Name.EDIT, dependencyCacheContainer);
        }
        return controller;
    }

    public EnvVariableController getEnvVariableController() {
        LOG.debug("*** getEnvVariableController ***");
        return this.envVariableController;
    }

    public GrepController getGrepController(final ZosConnection connection, final String target, final long timeout) {
        LOG.debug("*** getGrepController ***");
        var controller = (GrepController) controllers.get(ContainerType.Name.GREP);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.GREP);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, target, timeout)) {
            var service = new GrepService(connection, this.pathService, target, timeout);
            controller = new GrepController(service);
            dependencyCacheContainer = new Dependency(connection, target, timeout);
            this.controllers.put(ContainerType.Name.GREP, controller);
            this.dependencies.put(ContainerType.Name.GREP, dependencyCacheContainer);
        }
        return controller;
    }

    public ListingController getListingController(final ZosConnection connection, final TextTerminal<?> terminal,
                                                  final long timeout) {
        LOG.debug("*** getListingController ***");
        var controller = (ListingController) controllers.get(ContainerType.Name.LIST);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.LIST);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var dsnList = new DsnList(connection);
            var service = new ListingService(terminal, dsnList, timeout);
            controller = new ListingController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.LIST, controller);
            this.dependencies.put(ContainerType.Name.LIST, dependencyCacheContainer);
        }
        return controller;
    }

    public LocalFilesController getLocalFilesController() {
        LOG.debug("*** getLocalFilesController ***");
        var controller = (LocalFilesController) controllers.get(ContainerType.Name.LOCAL_FILE);
        if (controller == null) {
            var service = new LocalFileService();
            controller = new LocalFilesController(service);
            this.controllers.put(ContainerType.Name.LOCAL_FILE, controller);
        }
        return controller;
    }

    public MakeDirController getMakeDirController(final ZosConnection connection, final TextTerminal<?> terminal,
                                                  final long timeout) {
        LOG.debug("*** getMakeDirController ***");
        var controller = (MakeDirController) controllers.get(ContainerType.Name.MAKE_DIR);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.MAKE_DIR);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var dsnCreate = new DsnCreate(connection);
            var service = new MakeDirService(dsnCreate, timeout);
            controller = new MakeDirController(terminal, service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.MAKE_DIR, controller);
            this.dependencies.put(ContainerType.Name.MAKE_DIR, dependencyCacheContainer);
        }
        return controller;
    }

    public ProcessLstController getProcessLstController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getProcessLstController ***");
        var controller = (ProcessLstController) controllers.get(ContainerType.Name.PROCESS_LIST);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.PROCESS_LIST);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var service = new ProcessLstService(new JobGet(connection), timeout);
            controller = new ProcessLstController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.PROCESS_LIST, controller);
            this.dependencies.put(ContainerType.Name.PROCESS_LIST, dependencyCacheContainer);
        }
        return controller;
    }

    public PurgeController getPurgeController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getPurgeController ***");
        var controller = (PurgeController) controllers.get(ContainerType.Name.PURGE);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.PURGE);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var jobDelete = new JobDelete(connection);
            var jobGet = new JobGet(connection);
            var service = new PurgeService(jobDelete, jobGet, timeout);
            controller = new PurgeController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.PURGE, controller);
            this.dependencies.put(ContainerType.Name.PURGE, dependencyCacheContainer);
        }
        return controller;
    }

    public RenameController getRenameController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getRenameController ***");
        var controller = (RenameController) controllers.get(ContainerType.Name.RENAME);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.RENAME);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var service = new RenameService(connection, timeout);
            controller = new RenameController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.RENAME, controller);
            this.dependencies.put(ContainerType.Name.RENAME, dependencyCacheContainer);
        }
        return controller;
    }

    public SaveController getSaveController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getSaveController ***");
        var controller = (SaveController) controllers.get(ContainerType.Name.SAVE);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.SAVE);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var checkSumService = new CheckSumService();
            var service = new SaveService(new DsnWrite(connection), this.pathService, checkSumService, timeout);
            controller = new SaveController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.SAVE, controller);
            this.dependencies.put(ContainerType.Name.SAVE, dependencyCacheContainer);
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
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.STOP);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var issueConsole = new IssueConsole(connection);
            var service = new TerminateService(issueConsole, timeout);
            controller = new StopController(service,
                    ConfigSingleton.getInstance(), this.envVariableController);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.STOP, controller);
            this.dependencies.put(ContainerType.Name.STOP, dependencyCacheContainer);
        }
        return controller;
    }

    public SubmitController getSubmitController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getSubmitController ***");
        var controller = (SubmitController) controllers.get(ContainerType.Name.SUBMIT);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.SUBMIT);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var jobSubmit = new JobSubmit(connection);
            var service = new SubmitService(jobSubmit, timeout);
            controller = new SubmitController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.SUBMIT, controller);
            this.dependencies.put(ContainerType.Name.SUBMIT, dependencyCacheContainer);
        }
        return controller;
    }

    public TailController getTailController(final ZosConnection connection, final TextTerminal<?> terminal,
                                            final long timeout) {
        LOG.debug("*** getTailController ***");
        var controller = (TailController) controllers.get(ContainerType.Name.TAIL);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.TAIL);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var jobGet = new JobGet(connection);
            var service = new TailService(terminal, jobGet, timeout);
            controller = new TailController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.TAIL, controller);
            this.dependencies.put(ContainerType.Name.TAIL, dependencyCacheContainer);
        }
        return controller;
    }

    public TouchController getTouchController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getTouchController ***");
        var controller = (TouchController) controllers.get(ContainerType.Name.TOUCH);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.TOUCH);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var dsnWrite = new DsnWrite(connection);
            var dsnList = new DsnList(connection);
            var service = new TouchService(dsnWrite, dsnList, timeout);
            controller = new TouchController(service);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.TOUCH, controller);
            this.dependencies.put(ContainerType.Name.TOUCH, dependencyCacheContainer);
        }
        return controller;
    }

    public TsoController getTsoController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getTsoController ***");
        var controller = (TsoController) controllers.get(ContainerType.Name.TSO);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.TSO);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var issueTso = new IssueTso(connection);
            var service = new TsoService(issueTso, timeout);
            controller = new TsoController(service, ConfigSingleton.getInstance(), this.envVariableController);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.TSO, controller);
            this.dependencies.put(ContainerType.Name.TSO, dependencyCacheContainer);
        }
        return controller;
    }

    public UnameController getUnameController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getUnameController ***");
        var controller = (UnameController) controllers.get(ContainerType.Name.UNAME);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.UNAME);
        if (controller == null || dependencyCacheContainer != null &&
                dependencyCacheContainer.isValid(connection, timeout)) {
            var issueConsole = new IssueConsole(connection);
            var service = new UnameService(issueConsole, timeout);
            controller = new UnameController(service, ConfigSingleton.getInstance(), this.envVariableController);
            dependencyCacheContainer = new Dependency(connection, timeout);
            this.controllers.put(ContainerType.Name.UNAME, controller);
            this.dependencies.put(ContainerType.Name.UNAME, dependencyCacheContainer);
        }
        return controller;
    }

    public UsermodController getUsermodController(final ZosConnection connection, final int index) {
        LOG.debug("*** getUsermodController ***");
        var controller = (UsermodController) controllers.get(ContainerType.Name.USERMOD);
        if (controller == null) {
            var service = new UsermodService(connection, index);
            controller = new UsermodController(service);
            this.controllers.put(ContainerType.Name.USERMOD, controller);
        }
        return controller;
    }

    public UssController getUssController(final SshConnection connection) {
        LOG.debug("*** getUssController ***");
        var controller = (UssController) controllers.get(ContainerType.Name.USS);
        var dependencyCacheContainer = dependencies.get(ContainerType.Name.USS);
        if (controller == null || dependencyCacheContainer != null &&
                !(dependencyCacheContainer.isSshConnectionSame(connection))) {
            var service = new SshService(connection);
            controller = new UssController(service);
            dependencyCacheContainer = new Dependency(connection);
            this.controllers.put(ContainerType.Name.USS, controller);
            this.dependencies.put(ContainerType.Name.USS, dependencyCacheContainer);
        }
        return controller;
    }

}
