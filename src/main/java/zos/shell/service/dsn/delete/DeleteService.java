package zos.shell.service.dsn.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureUtil;
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

public class DeleteService {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteService.class);

    private final ZosConnection connection;
    private final long timeout;

    public DeleteService(final ZosConnection connection, final long timeout) {
        LOG.debug("*** DeleteService ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus delete(final String currDataSet, String target) {
        LOG.debug("*** delete ***");
        List<Member> members;

        var datasetMemberTarget = DatasetMember.getDatasetAndMember(target);
        // delete dataset(member) not in currDataset
        if (datasetMemberTarget != null) {
            return processRequest(datasetMemberTarget.getDataset(), datasetMemberTarget.getMember());
        }

        // delete currDataset(member)
        if (DsnUtil.isMember(target)) {
            if (currDataSet.isBlank()) {
                new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
            }

            try {
                members = new MemberListingService(new DsnList(connection), timeout).memberLst(currDataSet);
            } catch (ZosmfRequestException e) {
                var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
                return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
            }
            if (DsnUtil.getMembersByFilter(target, members).isEmpty()) {
                return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
            }
            return processRequest(currDataSet, target);
        }

        // handle sequential dataset
        if (DsnUtil.isDataset(target)) {
            return processRequest(target, null);
        }

        long numOfAsterisk = target.chars().filter(ch -> ch == '*').count();
        boolean copyWildCard = numOfAsterisk == 1 && DsnUtil.isMember(target.substring(0, target.indexOf("*")));

        // copy member wild card to dataset
        if (copyWildCard) {
            try {
                members = new MemberListingService(new DsnList(connection), timeout).memberLst(currDataSet);
            } catch (ZosmfRequestException e) {
                var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
                return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
            }
            if (members.isEmpty()) {
                return new ResponseStatus(Constants.DELETE_NOTHING_ERROR, false);
            }

            // transform target is a member string without * (a wild card)
            target = target.substring(0, target.indexOf("*"));
            members = DsnUtil.getMembersByStartsWithFilter(target, members);
            if (members.size() == 1 && !members.get(0).getMember().isBlank()) {
                return processRequest(currDataSet, members.get(0).getMember());
            }

            List<Future<ResponseStatus>> futures = new ArrayList<>();
            ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
            for (var member : members) {
                var name = member.getMember();
                var dsnDelete = new DsnDelete(connection);
                var future = new FutureDelete(dsnDelete, currDataSet, name);
                futures.add(pool.submit(future));
            }
            return FutureUtil.getFutureResponses(futures, pool, timeout, Constants.STRING_PAD_LENGTH);
        }

        String errMsg;
        if ("*".equals(target)) {
            errMsg = "no support for removal of all members for now, try again...";
        } else {
            errMsg = Constants.INVALID_ARGUMENTS;
        }

        return new ResponseStatus(errMsg, false);
    }

    private ResponseStatus processRequest(final String dataset, final String member) {
        LOG.debug("*** processRequest ***");
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        var dsnDelete = new DsnDelete(connection);
        var futureDelete = new FutureDelete(dsnDelete, dataset, member);
        Future<ResponseStatus> submit = pool.submit(futureDelete);
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
