package zos.shell.service.dsn.copy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCopy;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CopyCmd {

    private static final Logger LOG = LoggerFactory.getLogger(CopyCmd.class);

    private final ZosConnection connection;
    private final DsnList dsnList;
    private final ListParams listParams = new ListParams.Builder().build();
    private final long timeout;

    public CopyCmd(final ZosConnection connection, final DsnList dsnList, final long timeout) {
        LOG.debug("*** Copy ***");
        this.connection = connection;
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public ResponseStatus copy(final String dataset, final String[] params) {
        LOG.debug("*** copy ***");
        long count = params[1].chars().filter(ch -> ch == '*').count();
        if (count > 1) {
            return new ResponseStatus("invalid first argument, try again...", false);
        }

        final var result = new StringBuilder();

        if (params[1].contains("*") && Util.isMember(params[1].substring(0, params[1].indexOf("*")))) {

            List<Member> members;
            try {
                members = dsnList.getMembers(dataset, listParams);
            } catch (ZosmfRequestException e) {
                final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
                return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
            }

            final var index = params[1].indexOf("*");
            final var searchForMember = params[1].substring(0, index).toUpperCase();
            members = members.stream().filter(m -> {
                if (m.getMember().isPresent()) {
                    return m.getMember().get().startsWith(searchForMember);
                }
                return false;
            }).collect(Collectors.toList());

            if (members.isEmpty()) {
                return new ResponseStatus(Constants.COPY_NOTHING_WARNING, false);
            }

            final var toDataSetName = params[2];

            if (!Util.isDataSet(toDataSetName)) {
                return new ResponseStatus(Constants.INVALID_DATASET, false);
            }

            final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
            final var futures = new ArrayList<Future<ResponseStatus>>();
            members.forEach(m -> {
                if (m.getMember().isPresent()) {
                    DsnCopy dsnCopy = new DsnCopy(connection);
                    // dataset == fromDataSetName
                    futures.add(pool.submit(new FutureCopy(dsnCopy, dataset, toDataSetName, m.getMember().get())));
                }
            });

            futures.forEach(f -> {
                try {
                    final var responseStatus = f.get(timeout, TimeUnit.SECONDS);
                    result.append(responseStatus.getMessage()).append("\n");
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    result.append(Constants.TIMEOUT_MESSAGE).append("\n");
                    LOG.debug("error: " + e);
                }
            });

            pool.shutdownNow();
            return new ResponseStatus(result.toString(), true);
        }

        boolean isNonException = true;
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        final var submit = pool.submit(new FutureCopy(new DsnCopy(connection), dataset, params[2], params[1]));
        try {
            ResponseStatus responseStatus = submit.get(timeout, TimeUnit.SECONDS);
            result.append(responseStatus.getMessage());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            result.append(Constants.TIMEOUT_MESSAGE);
            isNonException = false;
            LOG.debug("error: " + e);
        }

        return new ResponseStatus(result.toString(), isNonException);
    }

}
