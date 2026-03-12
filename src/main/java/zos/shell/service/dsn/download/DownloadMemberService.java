package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.path.PathService;
import zos.shell.utility.FileUtil;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadMemberService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadMemberService.class);

    private final ZosConnection connection;
    private final PathService pathService;
    private final boolean isBinary;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public DownloadMemberService(final ZosConnection connection, final PathService pathService,
                                 final boolean isBinary, final long timeout) {
        LOG.debug("*** DownloadMemberService ***");
        this.connection = connection;
        this.pathService = pathService;
        this.isBinary = isBinary;
        this.timeout = timeout;
    }

    public List<ResponseStatus> downloadMember(final String dataset, String target) {
        LOG.debug("Downloading member {} from dataset {}", dataset, target);
        List<ResponseStatus> results = new ArrayList<>();
        Future<ResponseStatus> future = pool.submit(new FutureMemberDownload(
                new DsnGet(connection),
                pathService,
                dataset,
                target,
                isBinary
        ));

        ResponseStatus status = FutureUtil.getResponseStatus(future, timeout);
        results.add(status);
        if (status.isStatus() && status.getOptionalData() != null) {
            FileUtil.openFileLocation(new File(status.getOptionalData()).getAbsolutePath());
        }
        return results;
    }

    @Override
    public void close() {
        pool.shutdown();
    }

}
