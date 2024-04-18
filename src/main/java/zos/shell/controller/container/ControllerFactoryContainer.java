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
import zos.shell.service.dsn.download.Download;
import zos.shell.service.dsn.download.DownloadDsnService;
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

public class ControllerFactoryContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerFactoryContainer.class);

    private BrowseJobController browseJobController;
    private DependencyCacheContainer browseJobDependencyContainer;
    private CancelController cancelController;
    private DependencyCacheContainer cancelDependencyContainer;
    private ChangeConnController changeConnController;
    private ChangeDirController changeDirController;
    private DependencyCacheContainer changeDirDependencyContainer;
    private ChangeWinController changeWinController;
    private ConcatController concatController;
    private DependencyCacheContainer concatDependencyContainer;
    private ConsoleController consoleController;
    private DependencyCacheContainer consoleDependencyContainer;
    private CopyController copyController;
    private DependencyCacheContainer copyDependencyContainer;
    private CountController countController;
    private DependencyCacheContainer countDependencyContainer;
    private DeleteController deleteController;
    private DependencyCacheContainer deleteDependencyContainer;
    private DownloadDsnController downloadDsnController;
    private DependencyCacheContainer downloadDsnDependencyContainer;
    private DownloadJobController downloadJobController;
    private DependencyCacheContainer downloadJobDependencyContainer;
    private EchoController echoController;
    private EditController editController;
    private DependencyCacheContainer editDependencyContainer;
    private EnvVariableController envVariableController;
    private GrepController grepController;
    private DependencyCacheContainer grepDependencyContainer;
    private ListingController listingController;
    private DependencyCacheContainer listingDependencyContainer;
    private LocalFilesController localFilesController;
    private MakeDirController makeDirController;
    private DependencyCacheContainer makeDirDependencyContainer;
    private ProcessLstController processLstController;
    private DependencyCacheContainer processLstDependencyContainer;
    private PurgeController purgeController;
    private DependencyCacheContainer purgeDependencyContainer;
    private RenameController renameController;
    private DependencyCacheContainer renameDependencyContainer;
    private SaveController saveController;
    private DependencyCacheContainer saveDependencyContainer;
    private SearchCacheController searchCacheController;
    private StopController stopController;
    private DependencyCacheContainer stopDependencyContainer;
    private SubmitController submitController;
    private DependencyCacheContainer submitDependencyContainer;
    private TailController tailController;
    private DependencyCacheContainer tailDependencyContainer;
    private TouchController touchController;
    private DependencyCacheContainer touchDependencyContainer;
    private TsoController tsoController;
    private DependencyCacheContainer tsoDependencyContainer;
    private UnameController unameController;
    private DependencyCacheContainer unameDependencyContainer;
    private UssController ussController;
    private DependencyCacheContainer ussDependencyContainer;
    private PathService pathService;

    public ControllerFactoryContainer() {
        LOG.debug("*** ControllerFactoryContainer ***");
    }

    public BrowseJobController getBrowseJobController(final ZosConnection connection, boolean isAll,
                                                      final long timeout) {
        LOG.debug("*** getBrowseJobController ***");
        if (this.browseJobController == null ||
                (this.browseJobDependencyContainer != null && (
                        !(this.browseJobDependencyContainer.isZosConnectionSame(connection) &&
                                this.browseJobDependencyContainer.isTimeoutSame(timeout) &&
                                this.browseJobDependencyContainer.isToggleSame(isAll))))) {
            var jobGet = new JobGet(connection);
            var browseJobService = new BrowseLogService(jobGet, isAll, timeout);
            this.browseJobController = new BrowseJobController(browseJobService);
            this.browseJobDependencyContainer = new DependencyCacheContainer(connection, isAll, timeout);
        }
        return this.browseJobController;
    }

    public CancelController getCancelController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCancelController ***");
        if (this.cancelController == null ||
                (this.cancelDependencyContainer != null && (
                        !(this.cancelDependencyContainer.isZosConnectionSame(connection) &&
                                this.cancelDependencyContainer.isTimeoutSame(timeout))))) {

            var issueConsole = new IssueConsole(connection);
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            var cancelService = new TerminateService(issueConsole, timeout);
            this.cancelController = new CancelController(cancelService,
                    ConfigSingleton.getInstance(), this.envVariableController);
            this.cancelDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.cancelController;
    }

    public ChangeConnController getChangeConnController(final TextTerminal<?> terminal) {
        LOG.debug("*** getChangeConnController ***");
        if (this.changeConnController == null) {
            var changeConnService = new ChangeConnService(terminal);
            this.changeConnController = new ChangeConnController(changeConnService);
            return this.changeConnController;
        }
        return this.changeConnController;
    }

    public ChangeDirController getChangeDirController(final ZosConnection connection) {
        LOG.debug("*** getChangeDirController ***");
        if (this.changeDirController == null ||
                (this.changeDirDependencyContainer != null && (
                        !(this.changeDirDependencyContainer.isZosConnectionSame(connection))))) {
            var dsnList = new DsnList(connection);
            var changeDirService = new ChangeDirService(dsnList);
            this.changeDirController = new ChangeDirController(changeDirService);
            this.changeDirDependencyContainer = new DependencyCacheContainer(connection);
        }
        return this.changeDirController;
    }

    public ChangeWinController getChangeWinController(final TextTerminal<?> terminal) {
        LOG.debug("*** getChangeWinController ***");
        if (this.changeWinController == null) {
            var changeWinService = new ChangeWinService(terminal);
            this.changeWinController = new ChangeWinController(changeWinService);
            return this.changeWinController;
        }
        return this.changeWinController;
    }

    public ConcatController getConcatController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getConcatController ***");
        if (this.concatController == null ||
                (this.concatDependencyContainer != null && (
                        !(this.concatDependencyContainer.isZosConnectionSame(connection) &&
                                this.concatDependencyContainer.isTimeoutSame(timeout))))) {
            var dsnGet = new DsnGet(connection);
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            if (this.pathService == null) {
                this.pathService = new PathService(ConfigSingleton.getInstance(), ConnSingleton.getInstance(),
                        this.envVariableController);
            }
            var download = new Download(dsnGet, this.pathService, false);
            var concatService = new ConcatService(download, timeout);
            this.concatController = new ConcatController(concatService);
            this.concatDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.concatController;
    }

    public ConsoleController getConsoleController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getConsoleController ***");
        if (this.consoleController == null ||
                (this.consoleDependencyContainer != null && (
                        !(this.consoleDependencyContainer.isZosConnectionSame(connection) &&
                                this.consoleDependencyContainer.isTimeoutSame(timeout))))) {
            var consoleService = new ConsoleService(connection, timeout);
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            this.consoleController = new ConsoleController(consoleService,
                    ConfigSingleton.getInstance(), this.envVariableController);
            this.consoleDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.consoleController;
    }

    public CopyController getCopyController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCopyController ***");
        if (this.copyController == null ||
                (this.copyDependencyContainer != null && (
                        !(this.copyDependencyContainer.isZosConnectionSame(connection) &&
                                this.copyDependencyContainer.isTimeoutSame(timeout))))) {
            var copyService = new CopyService(connection, timeout);
            this.copyController = new CopyController(copyService);
            this.copyDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.copyController;
    }

    public CountController getCountController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCountController ***");
        if (this.countController == null ||
                (this.countDependencyContainer != null && (
                        !(this.countDependencyContainer.isZosConnectionSame(connection) &&
                                this.countDependencyContainer.isTimeoutSame(timeout))))) {
            var dsnList = new DsnList(connection);
            var countService = new CountService(dsnList, timeout);
            this.countController = new CountController(countService);
            this.countDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.countController;
    }

    public DeleteController getDeleteController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getDeleteController ***");
        if (this.deleteController == null ||
                (this.deleteDependencyContainer != null && (
                        !(this.deleteDependencyContainer.isZosConnectionSame(connection) &&
                                this.deleteDependencyContainer.isTimeoutSame(timeout))))) {
            var deleteService = new DeleteService(connection, timeout);
            this.deleteController = new DeleteController(deleteService);
            this.deleteDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.deleteController;
    }

    public DownloadDsnController getDownloadDsnController(final ZosConnection connection, boolean isBinary,
                                                          final long timeout) {
        LOG.debug("*** getDownloadDsnController ***");
        if (this.downloadDsnController == null ||
                (this.downloadDsnDependencyContainer != null && (
                        !(this.downloadDsnDependencyContainer.isZosConnectionSame(connection) &&
                                this.downloadDsnDependencyContainer.isTimeoutSame(timeout) &&
                                this.downloadDsnDependencyContainer.isToggleSame(isBinary))))) {
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            if (this.pathService == null) {
                this.pathService = new PathService(ConfigSingleton.getInstance(), ConnSingleton.getInstance(),
                        this.envVariableController);
            }
            var downloadDsnService = new DownloadDsnService(connection, this.pathService, isBinary, timeout);
            this.downloadDsnController = new DownloadDsnController(downloadDsnService);
            this.downloadDsnDependencyContainer = new DependencyCacheContainer(connection, isBinary, timeout);
        }
        return this.downloadDsnController;
    }

    public DownloadJobController getDownloadJobController(final ZosConnection connection, boolean isAll,
                                                          final long timeout) {
        LOG.debug("*** getDownloadJobController ***");
        if (this.downloadJobController == null ||
                (this.downloadJobDependencyContainer != null && (
                        !(this.downloadJobDependencyContainer.isZosConnectionSame(connection) &&
                                this.downloadJobDependencyContainer.isTimeoutSame(timeout) &&
                                this.downloadJobDependencyContainer.isToggleSame(isAll))))) {
            var jobGet = new JobGet(connection);
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            if (this.pathService == null) {
                this.pathService = new PathService(ConfigSingleton.getInstance(), ConnSingleton.getInstance(),
                        this.envVariableController);
            }
            var downloadJobService = new DownloadJobService(jobGet, this.pathService, isAll, timeout);
            this.downloadJobController = new DownloadJobController(downloadJobService);
            this.downloadJobDependencyContainer = new DependencyCacheContainer(connection, isAll, timeout);
        }
        return this.downloadJobController;
    }

    public EchoController getEchoController() {
        LOG.debug("*** getEchoController ***");
        if (this.echoController == null) {
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            var echoService = new EchoService(this.envVariableController);
            this.echoController = new EchoController(echoService);
        }
        return this.echoController;
    }

    public EditController getEditController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getEditController ***");
        if (this.editController == null ||
                (this.editDependencyContainer != null && (
                        !(this.editDependencyContainer.isZosConnectionSame(connection) &&
                                this.editDependencyContainer.isTimeoutSame(timeout))))) {
            var dsnGet = new DsnGet(connection);
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            if (this.pathService == null) {
                this.pathService = new PathService(ConfigSingleton.getInstance(), ConnSingleton.getInstance(),
                        this.envVariableController);
            }
            var download = new Download(dsnGet, this.pathService, false);
            var checkSumService = new CheckSumService();
            var editService = new EditService(download, this.pathService, checkSumService, timeout);
            this.editController = new EditController(editService);
            this.editDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.editController;
    }

    public EnvVariableController getEnvVariableController() {
        LOG.debug("*** getEnvVariableController ***");
        if (this.envVariableController == null) {
            var envVariableService = new EnvVariableService();
            this.envVariableController = new EnvVariableController(envVariableService);
            return this.envVariableController;
        }
        return this.envVariableController;
    }

    public GrepController getGrepController(final ZosConnection connection, final String target, final long timeout) {
        LOG.debug("*** getGrepController ***");
        if (this.grepController == null ||
                (this.grepDependencyContainer != null && (
                        !(this.grepDependencyContainer.isZosConnectionSame(connection) &&
                                this.grepDependencyContainer.isTimeoutSame(timeout) &&
                                this.grepDependencyContainer.isDataSame(target))))) {
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            if (this.pathService == null) {
                this.pathService = new PathService(ConfigSingleton.getInstance(), ConnSingleton.getInstance(),
                        this.envVariableController);
            }
            var grepService = new GrepService(connection, this.pathService, target, timeout);
            this.grepController = new GrepController(grepService);
            this.grepDependencyContainer = new DependencyCacheContainer(connection, target, timeout);
        }
        return this.grepController;
    }

    public ListingController getListingController(final ZosConnection connection, final TextTerminal<?> terminal,
                                                  final long timeout) {
        LOG.debug("*** getListingController ***");
        if (this.listingController == null ||
                (this.listingDependencyContainer != null && (
                        !(this.listingDependencyContainer.isZosConnectionSame(connection) &&
                                this.listingDependencyContainer.isTimeoutSame(timeout))))) {
            var dsnList = new DsnList(connection);
            var listingService = new ListingService(terminal, dsnList, timeout);
            this.listingController = new ListingController(listingService);
            this.listingDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.listingController;
    }

    public LocalFilesController getLocalFilesController() {
        LOG.debug("*** getLocalFilesController ***");
        if (this.localFilesController == null) {
            var localFilesService = new LocalFileService();
            this.localFilesController = new LocalFilesController(localFilesService);
            return this.localFilesController;
        }
        return this.localFilesController;
    }

    public MakeDirController getMakeDirController(final ZosConnection connection, final TextTerminal<?> terminal,
                                                  final long timeout) {
        LOG.debug("*** getMakeDirController ***");
        if (this.makeDirController == null ||
                (this.makeDirDependencyContainer != null && (
                        !(this.makeDirDependencyContainer.isZosConnectionSame(connection) &&
                                this.makeDirDependencyContainer.isTimeoutSame(timeout))))) {
            var dsnCreate = new DsnCreate(connection);
            var makeDirService = new MakeDirService(dsnCreate, timeout);
            this.makeDirController = new MakeDirController(terminal, makeDirService);
            this.makeDirDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.makeDirController;
    }

    public ProcessLstController getProcessLstController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getProcessLstController ***");
        if (this.processLstController == null ||
                (this.processLstDependencyContainer != null && (
                        !(this.processLstDependencyContainer.isZosConnectionSame(connection) &&
                                this.processLstDependencyContainer.isTimeoutSame(timeout))))) {
            var processLstService = new ProcessLstService(new JobGet(connection), timeout);
            this.processLstController = new ProcessLstController(processLstService);
            this.processLstDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.processLstController;
    }

    public PurgeController getPurgeController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getPurgeController ***");
        if (this.purgeController == null ||
                (this.purgeDependencyContainer != null && (
                        !(this.purgeDependencyContainer.isZosConnectionSame(connection) &&
                                this.purgeDependencyContainer.isTimeoutSame(timeout))))) {
            var jobDelete = new JobDelete(connection);
            var jobGet = new JobGet(connection);
            var purgeService = new PurgeService(jobDelete, jobGet, timeout);
            this.purgeController = new PurgeController(purgeService);
            this.purgeDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.purgeController;
    }

    public RenameController getRenameController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getRenameController ***");
        if (this.renameController == null ||
                (this.renameDependencyContainer != null && (
                        !(this.renameDependencyContainer.isZosConnectionSame(connection) &&
                                this.renameDependencyContainer.isTimeoutSame(timeout))))) {
            var renameService = new RenameService(connection, timeout);
            this.renameController = new RenameController(renameService);
            this.renameDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.renameController;
    }

    public SaveController getSaveController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getSaveController ***");
        if (this.saveController == null ||
                (this.saveDependencyContainer != null && (
                        !(this.saveDependencyContainer.isZosConnectionSame(connection) &&
                                this.saveDependencyContainer.isTimeoutSame(timeout))))) {
            var checkSumService = new CheckSumService();
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            if (this.pathService == null) {
                this.pathService = new PathService(ConfigSingleton.getInstance(), ConnSingleton.getInstance(),
                        this.envVariableController);
            }
            var saveService = new SaveService(new DsnWrite(connection), this.pathService, checkSumService, timeout);
            this.saveController = new SaveController(saveService);
            this.saveDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.saveController;
    }

    public SearchCacheController getSearchCacheController() {
        LOG.debug("*** getSearchCacheController ***");
        if (this.searchCacheController == null) {
            var searchCacheService = new SearchCacheService();
            this.searchCacheController = new SearchCacheController(searchCacheService);
            return this.searchCacheController;
        }
        return this.searchCacheController;
    }

    public StopController getStopController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getStopController ***");
        if (this.stopController == null ||
                (this.stopDependencyContainer != null && (
                        !(this.stopDependencyContainer.isZosConnectionSame(connection) &&
                                this.stopDependencyContainer.isTimeoutSame(timeout))))) {
            var issueConsole = new IssueConsole(connection);
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            var stopService = new TerminateService(issueConsole, timeout);
            this.stopController = new StopController(stopService,
                    ConfigSingleton.getInstance(), this.envVariableController);
            this.stopDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.stopController;
    }

    public SubmitController getSubmitController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getSubmitController ***");
        if (this.submitController == null ||
                (this.submitDependencyContainer != null && (
                        !(this.submitDependencyContainer.isZosConnectionSame(connection) &&
                                this.submitDependencyContainer.isTimeoutSame(timeout))))) {
            var jobSubmit = new JobSubmit(connection);
            var submitService = new SubmitService(jobSubmit, timeout);
            this.submitController = new SubmitController(submitService);
            this.submitDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.submitController;
    }

    public TailController getTailController(final ZosConnection connection, final TextTerminal<?> terminal,
                                            final long timeout) {
        LOG.debug("*** getTailController ***");
        if (this.tailController == null ||
                (this.tailDependencyContainer != null && (
                        !(this.tailDependencyContainer.isZosConnectionSame(connection) &&
                                this.tailDependencyContainer.isTimeoutSame(timeout))))) {
            var jobGet = new JobGet(connection);
            var tailService = new TailService(terminal, jobGet, timeout);
            this.tailController = new TailController(tailService);
            this.tailDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.tailController;
    }

    public TouchController getTouchController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getTouchController ***");
        if (this.touchController == null ||
                (this.touchDependencyContainer != null && (
                        !(this.touchDependencyContainer.isZosConnectionSame(connection) &&
                                this.touchDependencyContainer.isTimeoutSame(timeout))))) {
            var dsnWrite = new DsnWrite(connection);
            var dsnList = new DsnList(connection);
            var touchService = new TouchService(dsnWrite, dsnList, timeout);
            this.touchController = new TouchController(touchService);
            this.touchDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.touchController;
    }

    public TsoController getTsoController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getTsoController ***");
        if (this.tsoController == null ||
                (this.tsoDependencyContainer != null && (
                        !(this.tsoDependencyContainer.isZosConnectionSame(connection) &&
                                this.tsoDependencyContainer.isTimeoutSame(timeout))))) {
            var issueTso = new IssueTso(connection);
            var tsoService = new TsoService(issueTso, timeout);
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            this.tsoController = new TsoController(tsoService,
                    ConfigSingleton.getInstance(), this.getEnvVariableController());
            this.tsoDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.tsoController;
    }

    public UnameController getUnameController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getUnameController ***");
        if (this.unameController == null ||
                (this.unameDependencyContainer != null && (
                        !(this.unameDependencyContainer.isZosConnectionSame(connection) &&
                                this.unameDependencyContainer.isTimeoutSame(timeout))))) {
            var issueConsole = new IssueConsole(connection);
            var unameService = new UnameService(issueConsole, timeout);
            if (this.envVariableController == null) {
                var envVariableService = new EnvVariableService();
                this.envVariableController = new EnvVariableController(envVariableService);
            }
            this.unameController = new UnameController(unameService,
                    ConfigSingleton.getInstance(), this.envVariableController);
            this.unameDependencyContainer = new DependencyCacheContainer(connection, timeout);
        }
        return this.unameController;
    }

    public UsermodController getUsermodController(final ZosConnection connection, final int index) {
        LOG.debug("*** getUsermodController ***");
        var usermodService = new UsermodService(connection, index);
        return new UsermodController(usermodService);
    }

    public UssController getUssController(final SshConnection connection) {
        LOG.debug("*** getUssController ***");
        if (this.ussController == null ||
                (this.ussDependencyContainer != null &&
                        !(this.ussDependencyContainer.isSshConnectionSame(connection)))) {
            var sshService = new SshService(connection);
            this.ussController = new UssController(sshService);
            this.ussDependencyContainer = new DependencyCacheContainer(connection);
        }
        return this.ussController;
    }

}
