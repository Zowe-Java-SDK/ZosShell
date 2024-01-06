package zos.shell.service.dsn.copy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
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

        if (params[1].contains("*") && DsnUtil.isMember(params[1].substring(0, params[1].indexOf("*")))) {
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

            if (!DsnUtil.isDataSet(toDataSetName)) {
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
                } catch (InterruptedException | ExecutionException e) {
                    LOG.debug("error: " + e);
                    f.cancel(true);
                    result.append(e.getMessage() != null && !e.getMessage().isBlank() ?
                            e.getMessage() : Constants.EXECUTE_ERROR_MSG).append("\n");
                } catch (TimeoutException e) {
                    f.cancel(true);
                    result.append("timeout").append("\n");
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
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
            isNonException = false;
            submit.cancel(true);
            result.append(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG);
        } catch (TimeoutException e) {
            isNonException = false;
            submit.cancel(true);
            result.append(Constants.TIMEOUT_MESSAGE);
        } finally {
            pool.shutdown();
        }

        return new ResponseStatus(result.toString(), isNonException);
    }

}
