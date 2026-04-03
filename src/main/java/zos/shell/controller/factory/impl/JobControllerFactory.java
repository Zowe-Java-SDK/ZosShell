package zos.shell.controller.factory.impl;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.*;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.factory.AbstractDependencyControllerFactory;
import zos.shell.controller.factory.type.JobControllerType;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.job.browse.BrowseLogService;
import zos.shell.service.job.download.DownloadJobService;
import zos.shell.service.job.processlst.ProcessLstService;
import zos.shell.service.job.purge.PurgeService;
import zos.shell.service.job.submit.SubmitService;
import zos.shell.service.job.tail.TailService;
import zos.shell.service.job.terminate.TerminateService;
import zos.shell.service.path.PathService;
import zos.shell.singleton.ConnSingleton;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.methods.ConsoleCmd;
import zowe.client.sdk.zosjobs.methods.JobDelete;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.methods.JobSubmit;

public class JobControllerFactory extends AbstractDependencyControllerFactory<JobControllerType.Name> {

    private static final Logger LOG = LoggerFactory.getLogger(JobControllerFactory.class);

    public BrowseJobController getBrowseJobController(final ZosConnection connection,
                                                      final boolean isAll,
                                                      final long timeout) {
        LOG.debug("*** getBrowseJobController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .toggle(isAll)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                JobControllerType.Name.BROWSE_JOB,
                BrowseJobController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var jobGet = new JobGet(connection);
                    var browseLogService = new BrowseLogService(jobGet, isAll, timeout);
                    return new BrowseJobController(browseLogService, envVariableController, dependency);
                }
        );
    }

    public CancelController getCancelController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCancelController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                JobControllerType.Name.CANCEL,
                CancelController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var issueConsole = new ConsoleCmd(connection);
                    var terminateService = new TerminateService(issueConsole, timeout);
                    return new CancelController(terminateService, envVariableController, dependency);
                }
        );
    }

    public DownloadJobController getDownloadJobController(final ZosConnection connection,
                                                          final boolean isAll,
                                                          final String jobId,
                                                          final long timeout) {
        LOG.debug("*** getDownloadJobController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .toggle(isAll)
                .data(jobId)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                JobControllerType.Name.DOWNLOAD_JOB,
                DownloadJobController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var pathService = new PathService(ConnSingleton.getInstance(), envVariableController);
                    var jobGet = new JobGet(connection);
                    var downloadJobService = new DownloadJobService(jobGet, pathService, isAll, jobId, timeout);
                    return new DownloadJobController(downloadJobService, dependency);
                }
        );
    }

    public ProcessLstController getProcessLstController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getProcessLstController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                JobControllerType.Name.PROCESS_LIST,
                ProcessLstController.class,
                dependency,
                () -> {
                    var processLstService = new ProcessLstService(new JobGet(connection), timeout);
                    return new ProcessLstController(processLstService, dependency);
                }
        );
    }

    public PurgeController getPurgeController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getPurgeController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                JobControllerType.Name.PURGE,
                PurgeController.class,
                dependency,
                () -> {
                    var jobDelete = new JobDelete(connection);
                    var jobGet = new JobGet(connection);
                    var purgeService = new PurgeService(jobDelete, jobGet, timeout);
                    return new PurgeController(purgeService, dependency);
                }
        );
    }

    public StopController getStopController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getStopController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                JobControllerType.Name.STOP,
                StopController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var issueConsole = new ConsoleCmd(connection);
                    var terminateService = new TerminateService(issueConsole, timeout);
                    return new StopController(terminateService, envVariableController, dependency);
                }
        );
    }

    public SubmitController getSubmitController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getSubmitController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                JobControllerType.Name.SUBMIT,
                SubmitController.class,
                dependency,
                () -> {
                    var jobSubmit = new JobSubmit(connection);
                    var submitService = new SubmitService(jobSubmit, timeout);
                    return new SubmitController(submitService, dependency);
                }
        );
    }

    public TailController getTailController(final ZosConnection connection,
                                            final TextTerminal<?> terminal,
                                            final long timeout) {
        LOG.debug("*** getTailController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                JobControllerType.Name.TAIL,
                TailController.class,
                dependency,
                () -> {
                    var jobGet = new JobGet(connection);
                    var tailService = new TailService(terminal, jobGet, timeout);
                    return new TailController(tailService, dependency);
                }
        );
    }

}
