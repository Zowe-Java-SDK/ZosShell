package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.path.PathService;
import zos.shell.utility.FileUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DownloadPdsMemberService {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadPdsMemberService.class);

    private final ZosConnection connection;
    private final PathService pathService;
    private final boolean isBinary;
    private final long timeout;

    public DownloadPdsMemberService(final ZosConnection connection, final PathService pathService, final boolean isBinary,
                                    final long timeout) {
        LOG.debug("*** DownloadPdsMemberService ***");
        this.connection = connection;
        this.pathService = pathService;
        this.isBinary = isBinary;
        this.timeout = timeout;
    }

    public List<ResponseStatus> downloadPdsMember(final DatasetMember dataSetMember) {
        LOG.debug("*** downloadPdsMember ***");
        List<ResponseStatus> results = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = null;

        try {
            submit = pool.submit(new FutureMemberDownload(new DsnGet(connection), pathService,
                    dataSetMember.getDataset(), dataSetMember.getMember(), isBinary));
            results.add(submit.get(timeout, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("exception error: {}", String.valueOf(e));
            submit.cancel(true);
            results.add(new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG, false));
        } catch (TimeoutException e) {
            submit.cancel(true);
            results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
        } finally {
            pool.shutdown();
        }

        if (results.get(0).isStatus()) {
            var file = new File(results.get(0).getOptionalData());
            FileUtil.openFileLocation(file.getAbsolutePath());
            return results;
        }

        return results;
    }

}