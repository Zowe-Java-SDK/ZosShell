package zos.shell.service.dsn.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DataSetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnDelete;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DeleteCmd {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteCmd.class);

    private final ZosConnection connection;
    private final DsnList dsnList;
    private final ListParams params = new ListParams.Builder().build();
    private final long timeout;

    public DeleteCmd(final ZosConnection connection, final DsnList dsnList, final long timeout) {
        LOG.debug("*** DeleteCmd ***");
        this.connection = connection;
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public ResponseStatus delete(final String currDataSet, final String param) {
        LOG.debug("*** delete ***");
        try {
            List<Member> members;
            final var result = new StringBuilder();

            // member wild card delete operation
            if (param.contains("*") && param.chars().filter(ch -> ch == '*').count() == 1) {
                String lookForStr = "";

                if (param.length() > 1) {
                    final var index = param.indexOf('*');
                    lookForStr = param.substring(0, index).toUpperCase();
                }

                if (isCurrDataSetEmpty(currDataSet)) {
                    new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
                }

                members = dsnList.getMembers(currDataSet, params);

                if (!lookForStr.isBlank()) {
                    String finalLookForStr = lookForStr;
                    members = members.stream()
                            .filter(i -> i.getMember().orElse("")
                                    .startsWith(finalLookForStr))
                            .collect(Collectors.toList());
                }

                if (members.isEmpty()) {
                    return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
                }

                final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
                final var futureLst = new ArrayList<Future<ResponseStatus>>();
                members.stream().filter(m -> m.getMember().isPresent()).forEach(m -> {
                    DsnDelete dsnDelete = new DsnDelete(connection);
                    futureLst.add(pool.submit(new FutureDelete(dsnDelete, currDataSet, m.getMember().get())));
                });

                futureLst.forEach(f -> processResult(result, f));
                pool.shutdown();
                return new ResponseStatus(result.toString(), true);
            }

            final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

            // handle member
            if (DsnUtil.isMember(param)) {
                if (isCurrDataSetEmpty(currDataSet)) {
                    new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
                }

                members = dsnList.getMembers(currDataSet, params);
                if (members.stream().noneMatch(m -> param.equalsIgnoreCase(m.getMember().orElse("")))) {
                    return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
                }

                final var future = pool.submit(new FutureDelete(new DsnDelete(connection), currDataSet, param));
                processResult(result, future);
                pool.shutdown();
                return new ResponseStatus(result.toString(), true);
            }

            // handle dataset(member) notation
            if (param.contains("(") && param.contains(")")) {
                final var dataSetMember = DataSetMember.getDatasetAndMember(param);
                if (dataSetMember == null) {
                    return new ResponseStatus(Constants.DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR, false);
                }

                final var future = pool.submit(new FutureDelete(new DsnDelete(connection),
                        dataSetMember.getDataSet(), dataSetMember.getMember()));
                processResult(result, future);
                pool.shutdown();
                return new ResponseStatus(result.toString(), true);
            }

            // handle sequential dataset
            if (DsnUtil.isDataSet(param)) {
                final var future = pool.submit(new FutureDelete(new DsnDelete(connection), param));
                processResult(result, future);
                pool.shutdown();
                return new ResponseStatus(result.toString(), true);
            }

            pool.shutdown();
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(Constants.DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR, false);
    }

    private void processResult(final StringBuilder result, final Future<ResponseStatus> future) {
        LOG.debug("*** processResult ***");
        try {
            final var responseStatus = future.get(timeout, TimeUnit.SECONDS);
            if (responseStatus.isStatus()) {
                result.append(responseStatus.getMessage()).append("deleted.").append("\n");
            } else {
                result.append(responseStatus.getMessage()).append("\n");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            result.append(Constants.TIMEOUT_MESSAGE);
            LOG.debug("error: " + e);
        }
    }

    private boolean isCurrDataSetEmpty(final String currDataSet) {
        LOG.debug("*** isCurrDataSetEmpty ***");
        return currDataSet.isBlank();
    }

}
