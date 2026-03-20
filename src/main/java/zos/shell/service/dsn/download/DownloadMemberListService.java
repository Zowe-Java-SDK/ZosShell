package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.controller.EnvVariableController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.path.PathService;
import zos.shell.singleton.ConnSingleton;
import zos.shell.utility.FutureUtil;
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
        List<Future<ResponseStatus>> futures = submitDownloads(dataset, members);
        List<ResponseStatus> results = new ArrayList<>(futures.size());

        for (Future<ResponseStatus> future : futures) {
            try {
                results.add(future.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                future.cancel(true);
                Thread.currentThread().interrupt();
                results.add(new ResponseStatus(FutureUtil.getErrorMessage(e), false));
                break;
            } catch (ExecutionException e) {
                future.cancel(true);
                results.add(new ResponseStatus(FutureUtil.getErrorMessage(e), false));
            } catch (TimeoutException e) {
                future.cancel(true);
                results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
            }
        }

        return results;
    }

    private List<Future<ResponseStatus>> submitDownloads(final String dataset,
                                                         final List<Member> members) {
        LOG.debug("*** submitDownloads ***");
        List<Future<ResponseStatus>> futures = new ArrayList<>(members.size());
        EnvVariableService envVariableService = new EnvVariableService();
        EnvVariableController envVariableController = new EnvVariableController(envVariableService);

        for (var member : members) {
            String memberName = member.getMember();
            if (memberName == null || memberName.isBlank()) {
                continue;
            }

            futures.add(pool.submit(new FutureMemberDownload(
                    new DsnGet(connection),
                    new PathService(
                            ConnSingleton.getInstance(),
                            envVariableController
                    ),
                    dataset,
                    memberName,
                    isBinary
            )));
        }

        return futures;
    }

    @Override
    public void close() {
        LOG.debug("*** close ***");
        pool.shutdown();
    }

}
