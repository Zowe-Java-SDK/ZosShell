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

public class DownloadMemberListService {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadMemberListService.class);

    private final ZosConnection connection;
    private final boolean isBinary;
    private final long timeout;

    public DownloadMemberListService(final ZosConnection connection, final boolean isBinary,
                                     final long timeout) {
        LOG.debug("*** DownloadMemberListService ***");
        this.connection = connection;
        this.isBinary = isBinary;
        this.timeout = timeout;
    }

    public List<ResponseStatus> downloadMembers(final String dataset, final List<Member> members) {
        LOG.debug("*** downloadMembers ***");
        List<ResponseStatus> results = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        var futures = new ArrayList<Future<ResponseStatus>>();

        try {
            for (var member : members) {
                if (!member.getMember().isBlank()) {
                    String name = member.getMember();
                    futures.add(pool.submit(new FutureMemberDownload(new DsnGet(connection),
                            new PathService(ConnSingleton.getInstance(),
                                    new EnvVariableController(new EnvVariableService())), dataset, name, isBinary)));
                }
            }

            for (var future : futures) {
                try {
                    results.add(future.get(timeout, TimeUnit.SECONDS));
                } catch (InterruptedException | ExecutionException e) {
                    LOG.debug(String.valueOf(e));
                    future.cancel(true);
                    results.add(new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                            e.getMessage() : Constants.EXECUTE_ERROR_MSG, false));
                } catch (TimeoutException e) {
                    future.cancel(true);
                    results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
                }
            }
        } finally {
            pool.shutdown();
        }

        return results;
    }

}
