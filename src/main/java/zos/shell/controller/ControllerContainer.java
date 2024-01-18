package zos.shell.controller;

import org.beryx.textio.TextTerminal;
import zos.shell.service.change.ChangeConnService;
import zos.shell.service.change.ChangeDirService;
import zos.shell.service.change.ChangeWinService;
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
import zos.shell.service.search.SearchCacheService;
import zos.shell.service.tso.TsoService;
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

public class ControllerContainer {

    private BrowseJobController browseJobController;
    private CancelController cancelController;
    private ChangeConnController changeConnController;
    private ChangeDirController changeDirService;
    private ChangeWinController changeWinController;
    private ConcatController concatController;
    private ConsoleController consoleController;
    private CopyController copyController;
    private CountController countController;
    private DeleteController deleteController;
    private DownloadDsnController downloadDsnController;
    private DownloadJobController downloadJobController;
    private EditController editController;
    private EnvVariableController envVariableController;
    private GrepController grepController;
    private ListingController listingController;
    private LocalFilesController localFilesController;
    private MakeDirController makeDirController;
    private ProcessLstController processLstController;
    private PurgeController purgeController;
    private SaveController saveController;
    private SearchCacheController searchCacheController;
    private StopController stopController;
    private SubmitController submitController;
    private TailController tailController;
    private TouchController touchController;
    private TsoController tsoController;
    private UnameController unameController;
    private UssController ussController;

    public ControllerContainer() {
    }

    public BrowseJobController getBrowseJobController(final ZosConnection connection, boolean isAll, final long timeout) {
        var jobGet = new JobGet(connection);
        var browseJobService = new BrowseLogService(jobGet, isAll, timeout);
        return new BrowseJobController(browseJobService);
    }

    public CancelController getCancelController(final ZosConnection connection, final long timeout) {
        var issueConsole = new IssueConsole(connection);
        var cancelService = new TerminateService(issueConsole, timeout);
        return new CancelController(cancelService);
    }

    public ChangeConnController getChangeConnController(final TextTerminal<?> terminal) {
        if (this.changeConnController == null) {
            var changeConnService = new ChangeConnService(terminal);
            return new ChangeConnController(changeConnService);
        } else {
            return this.changeConnController;
        }
    }

    public ChangeDirController getChangeDirController(final ZosConnection connection) {
        var dsnList = new DsnList(connection);
        var changeDirService = new ChangeDirService(dsnList);
        return new ChangeDirController(changeDirService);
    }

    public ChangeWinController getChangeWinController(final TextTerminal<?> terminal) {
        if (this.changeWinController == null) {
            var changeWinService = new ChangeWinService(terminal);
            return new ChangeWinController(changeWinService);
        } else {
            return this.changeWinController;
        }
    }

    public ConcatController getConcatController(final ZosConnection connection, final long timeout) {
        var dsnGet = new DsnGet(connection);
        var download = new Download(dsnGet, false);
        var concatService = new ConcatService(download, timeout);
        return new ConcatController(concatService);
    }

    public ConsoleController getConsoleController(final ZosConnection connection, final long timeout) {
        var consoleService = new ConsoleService(connection, timeout);
        return new ConsoleController(consoleService);
    }

    public CopyController getCopyController(final ZosConnection connection, final long timeout) {
        var copyService = new CopyService(connection, timeout);
        return new CopyController(copyService);
    }

    public CountController getCountController(final ZosConnection connection, final long timeout) {
        var dsnList = new DsnList(connection);
        var countService = new CountService(dsnList, timeout);
        return new CountController(countService);
    }

    public DeleteController getDeleteController(final ZosConnection connection, final long timeout) {
        var deleteService = new DeleteService(connection, timeout);
        return new DeleteController(deleteService);
    }

    public DownloadDsnController getDownloadDsnController(final ZosConnection connection, boolean isBinary,
                                                          final long timeout) {
        var downloadDsnService = new DownloadDsnService(connection, isBinary, timeout);
        return new DownloadDsnController(downloadDsnService);
    }

    public DownloadJobController getDownloadJobController(final ZosConnection connection, boolean isAll,
                                                          final long timeout) {
        var jobGet = new JobGet(connection);
        var downloadJobService = new DownloadJobService(jobGet, isAll, timeout);
        return new DownloadJobController(downloadJobService);
    }

    public EditController getEditController(final ZosConnection connection, final long timeout) {
        var dsnGet = new DsnGet(connection);
        var download = new Download(dsnGet, false);
        var editService = new EditService(download, timeout);
        return new EditController(editService);
    }

    public EnvVariableController getEnvVariableController() {
        if (this.envVariableController == null) {
            var envVariableService = new EnvVariableService();
            return new EnvVariableController(envVariableService);
        } else {
            return this.envVariableController;
        }
    }

    public GrepController getGrepController(final ZosConnection connection, final String target, final long timeout) {
        var grepService = new GrepService(connection, target, timeout);
        return new GrepController(grepService);
    }

    public ListingController getListingController(final ZosConnection connection, final TextTerminal<?> terminal,
                                                  final long timeout) {
        var dsnList = new DsnList(connection);
        var listingService = new ListingService(terminal, dsnList, timeout);
        return new ListingController(listingService);
    }

    public LocalFilesController getLocalFilesController() {
        if (this.localFilesController == null) {
            var localFilesService = new LocalFileService();
            return new LocalFilesController(localFilesService);
        } else {
            return this.localFilesController;
        }
    }

    public MakeDirController getMakeDirController(final ZosConnection connection, final TextTerminal<?> terminal,
                                                  final long timeout) {
        var dsnCreate = new DsnCreate(connection);
        var makeDirService = new MakeDirService(dsnCreate, timeout);
        return new MakeDirController(terminal, makeDirService);
    }

    public ProcessLstController getProcessLstController(final ZosConnection connection, final long timeout) {
        var processLstService = new ProcessLstService(new JobGet(connection), timeout);
        return new ProcessLstController(processLstService);
    }

    public PurgeController getPurgeController(final ZosConnection connection, final long timeout) {
        var jobDelete = new JobDelete(connection);
        var jobGet = new JobGet(connection);
        var purgeService = new PurgeService(jobDelete, jobGet, timeout);
        return new PurgeController(purgeService);
    }

    public SaveController getSaveController(final ZosConnection connection, final long timeout) {
        var saveService = new SaveService(new DsnWrite(connection), timeout);
        return new SaveController(saveService);
    }

    public SearchCacheController getSearchCacheController() {
        if (this.searchCacheController == null) {
            var searchCacheService = new SearchCacheService();
            return new SearchCacheController(searchCacheService);
        } else {
            return this.searchCacheController;
        }
    }

    public StopController getStopController(final ZosConnection connection, final long timeout) {
        var issueConsole = new IssueConsole(connection);
        var stopService = new TerminateService(issueConsole, timeout);
        return new StopController(stopService);
    }

    public SubmitController getSubmitController(final ZosConnection connection, final long timeout) {
        var jobSubmit = new JobSubmit(connection);
        var submitService = new SubmitService(jobSubmit, timeout);
        return new SubmitController(submitService);
    }

    public TailController getTailController(final ZosConnection connection, final TextTerminal<?> terminal,
                                            final long timeout) {
        var jobGet = new JobGet(connection);
        var tailService = new TailService(terminal, jobGet, timeout);
        return new TailController(tailService);
    }

    public TouchController getTouchController(final ZosConnection connection, final long timeout) {
        var dsnWrite = new DsnWrite(connection);
        var dsnList = new DsnList(connection);
        var touchService = new TouchService(dsnWrite, dsnList, timeout);
        return new TouchController(touchService);
    }

    public TsoController getTsoController(final ZosConnection connection, final String acctNum, final long timeout) {
        var issueTso = new IssueTso(connection);
        var tsoService = new TsoService(issueTso, acctNum, timeout);
        return new TsoController(tsoService);
    }

    public UnameController getUnameController(final ZosConnection connection, final long timeout) {
        var consoleService = new ConsoleService(connection, timeout);
        return new UnameController(consoleService);
    }

    public UssController getUssController(final SshConnection connection) {
        var sshService = new SshService(connection);
        return new UssController(sshService);
    }
}
