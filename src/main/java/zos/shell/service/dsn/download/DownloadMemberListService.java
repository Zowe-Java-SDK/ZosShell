package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.controller.EnvVariableController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.path.PathService;
import zos.shell.singleton.ConnSingleton;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DownloadMemberListService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadMemberListService.class);

    private final ZosConnection connection;
    private final boolean isBinary;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public DownloadMemberListService(final ZosConnection connection, final boolean isBinary,
                                     final long timeout) {
        LOG.debug("*** DownloadMemberListService ***");
        this.connection = connection;
        this.isBinary = isBinary;
        this.timeout = timeout;
    }

    public List<ResponseStatus> downloadMembers(final String dataset, final List<Member> members) {
        LOG.debug("Downloading {} members from dataset: {}", members.size(), dataset);

        final List<Future<ResponseStatus>> futures = submitDownloads(dataset, members);
        final List<ResponseStatus> results = new ArrayList<>(futures.size());

        for (Future<ResponseStatus> future : futures) {
            try {
                results.add(future.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException e) {
                LOG.debug("Exception downloading member list", e);
                future.cancel(true);
                results.add(new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                        e.getMessage() : Constants.EXECUTE_ERROR_MSG, false));
            } catch (TimeoutException e) {
                LOG.debug("Timeout downloading member list", e);
                future.cancel(true);
                results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
            }
        }

        return results;
    }

    private List<Future<ResponseStatus>> submitDownloads(final String dataset,
                                                         final List<Member> members) {
        final List<Future<ResponseStatus>> futures = new ArrayList<>();
        final DsnGet dsnGet = new DsnGet(connection);
        final EnvVariableService envVariableService = new EnvVariableService();
        final EnvVariableController envVariableController = new EnvVariableController(envVariableService);
        final PathService pathService = new PathService(ConnSingleton.getInstance(), envVariableController);

        for (Member member : members) {
            final String memberName = member.getMember();
            if (memberName == null || memberName.isBlank()) {
                continue;
            }

            futures.add(pool.submit(
                    new FutureMemberDownload(
                            dsnGet,
                            pathService,
                            dataset,
                            memberName,
                            isBinary
                    )
            ));
        }

        return futures;
    }

    @Override
    public void close() {
        pool.shutdown();
    }

}
