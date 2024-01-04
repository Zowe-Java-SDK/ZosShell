package zos.shell.service.dsn.count;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.datasetlst.FutureDatasetLst;
import zos.shell.service.memberlst.FutureMemberLst;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class CountCmd {

    private static final Logger LOG = LoggerFactory.getLogger(CountCmd.class);

    private final DsnList dsnList;
    private final long timeout;

    public CountCmd(final DsnList dsnList, final long timeout) {
        LOG.debug("*** CountCmd ***");
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public ResponseStatus count(final String dataset, final String filter) {
        LOG.debug("*** count ***");
        final var dataSetCount = new AtomicInteger();
        List<Dataset> datasets = new ArrayList<>();
        List<Member> members = new ArrayList<>();

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

        if ("members".equalsIgnoreCase(filter)) {
            final var submit = pool.submit(new FutureMemberLst(this.dsnList, dataset));
            try {
                members = submit.get(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                submit.cancel(true);
                LOG.debug("error: " + e);
                return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
            } finally {
                pool.shutdown();
            }
        }

        if ("datasets".equalsIgnoreCase(filter)) {
            final var submit = pool.submit(new FutureDatasetLst(this.dsnList, dataset));
            try {
                datasets = submit.get(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                submit.cancel(true);
                LOG.debug("error: " + e);
                return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
            } finally {
                pool.shutdown();
            }
        }

        datasets.forEach(item -> {
            if (!item.getDsname().orElse("n\\a").equalsIgnoreCase(dataset)) {
                dataSetCount.getAndIncrement();
            }
        });

        return new ResponseStatus(String.valueOf(members.size() + dataSetCount.get()), true);
    }

}
