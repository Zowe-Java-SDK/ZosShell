package zos.shell.service.dsn.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureResponseUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnDelete;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DeleteService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteService.class);

    private final ZosConnection connection;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
    private final ExecutorService bulkPool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);

    public DeleteService(final ZosConnection connection, final long timeout) {
        LOG.debug("*** DeleteService ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus delete(final String currDataSet, final String target) {
        LOG.debug("*** delete ***");

        final var datasetMemberTarget = DatasetMember.getDatasetAndMember(target);

        if (datasetMemberTarget != null) {
            return processRequest(datasetMemberTarget.getDataset(), datasetMemberTarget.getMember());
        }

        if (DsnUtil.isMember(target)) {
            return deleteMemberFromCurrentDataset(currDataSet, target);
        }

        if (DsnUtil.isDataset(target)) {
            return processRequest(target, null);
        }

        if (isSingleMemberWildcard(target)) {
            return deleteWildcardMembers(currDataSet, target);
        }

        if ("*".equals(target)) {
            return new ResponseStatus("no support for removal of all members for now, try again...", false);
        }

        return new ResponseStatus(Constants.INVALID_ARGUMENTS, false);
    }

    private ResponseStatus deleteMemberFromCurrentDataset(final String currDataSet, final String target) {
        if (currDataSet == null || currDataSet.isBlank()) {
            return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
        }

        final List<Member> members;
        try {
            members = listMembers(currDataSet);
        } catch (ZosmfRequestException e) {
            return buildErrorResponse(e);
        }

        if (DsnUtil.getMembersByFilter(target, members).isEmpty()) {
            return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
        }

        return processRequest(currDataSet, target);
    }

    private ResponseStatus deleteWildcardMembers(final String currDataSet, final String target) {
        if (currDataSet == null || currDataSet.isBlank()) {
            return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
        }

        List<Member> members;
        try {
            members = listMembers(currDataSet);
        } catch (ZosmfRequestException e) {
            return buildErrorResponse(e);
        }

        if (members.isEmpty()) {
            return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
        }

        final String memberPrefix = target.substring(0, target.indexOf('*'));
        members = DsnUtil.getMembersByStartsWithFilter(memberPrefix, members);

        if (members.isEmpty()) {
            return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
        }

        if (members.size() == 1 && !members.get(0).getMember().isBlank()) {
            return processRequest(currDataSet, members.get(0).getMember());
        }

        return processWildcardDeletes(currDataSet, members);
    }

    private List<Member> listMembers(final String dataset) throws ZosmfRequestException {
        try (var memberListingService = new MemberListingService(new DsnList(connection), timeout)) {
            return memberListingService.listMembers(dataset);
        }
    }

    private ResponseStatus processWildcardDeletes(final String dataset, final List<Member> members) {
        LOG.debug("*** processWildcardDeletes ***");

        final List<Future<ResponseStatus>> futures = new ArrayList<>();

        for (Member member : members) {
            futures.add(bulkPool.submit(new FutureDelete(
                    new DsnDelete(connection),
                    dataset,
                    member.getMember()
            )));
        }

        return FutureResponseUtil.getFutureResponses(futures, timeout, Constants.STRING_PAD_LENGTH);
    }

    private boolean isSingleMemberWildcard(final String target) {
        final long numOfAsterisk = target.chars().filter(ch -> ch == '*').count();
        return numOfAsterisk == 1
                && target.indexOf('*') > 0
                && DsnUtil.isMember(target.substring(0, target.indexOf('*')));
    }

    private ResponseStatus processRequest(final String dataset, final String member) {
        LOG.debug("*** processRequest ***");
        final Future<ResponseStatus> future = pool.submit(
                new FutureDelete(new DsnDelete(connection), dataset, member)
        );
        return FutureResponseUtil.waitForResult(future, timeout);
    }

    private ResponseStatus buildErrorResponse(final ZosmfRequestException e) {
        final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
        return new ResponseStatus(errMsg != null ? errMsg : e.getMessage(), false);
    }

    @Override
    public void close() {
        pool.shutdown();
        bulkPool.shutdown();
    }

}
