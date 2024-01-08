package zos.shell.service.dsn.copy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DataSetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberLst;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCopy;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CopyCmd {

    private static final Logger LOG = LoggerFactory.getLogger(CopyCmd.class);

    private final ZosConnection connection;
    private final long timeout;

    public CopyCmd(final ZosConnection connection, final long timeout) {
        LOG.debug("*** Copy ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus copy(final String currDataSet, final String[] params) {
        LOG.debug("*** copy ***");
        var fromDataSetName = "";
        var toDataSetName = "";

        final var firstParam = params[1].toUpperCase();
        final var secondParam = params[2].toUpperCase();

        final var datasetMemberFirstParam = DataSetMember.getDatasetAndMember(firstParam);
        if (datasetMemberFirstParam != null) {
            fromDataSetName = datasetMemberFirstParam.getDataSet() + "(" + datasetMemberFirstParam.getMember() + ")";
        }

        final var datasetMemberSecondParam = DataSetMember.getDatasetAndMember(secondParam);
        if (datasetMemberSecondParam != null) {
            toDataSetName = datasetMemberSecondParam.getDataSet() + "(" + datasetMemberSecondParam.getMember() + ")";
        }

        // copy dataset(member) to dataset(member)
        if (datasetMemberFirstParam != null && datasetMemberSecondParam != null) {
            return processRequest(fromDataSetName, toDataSetName, false);
        }

        // copy dataset to dataset
        if (DsnUtil.isDataSet(firstParam) && DsnUtil.isDataSet(secondParam)) {
            return processRequest(firstParam, secondParam, false);
        }

        if (DsnUtil.isMember(firstParam)) {
            if (currDataSet.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }
            fromDataSetName = currDataSet + "(" + firstParam + ")";
        }

        if (DsnUtil.isMember(secondParam)) {
            if (currDataSet.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }
            toDataSetName = currDataSet + "(" + secondParam + ")";
        }

        // copy currDataSet(member) to currDataSet(member)
        if (!fromDataSetName.isBlank() && !toDataSetName.isBlank()) {
            return processRequest(fromDataSetName, toDataSetName, false);
        }

        // copy ./* to dataset
        if (".".equals(firstParam) || "*".equals(firstParam)) {
            if (currDataSet.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }
            fromDataSetName = currDataSet;
            if (DsnUtil.isDataSet(secondParam)) {
                toDataSetName = secondParam;
            } else {
                return new ResponseStatus("specify valid dataset destination, try again...", false);
            }
            return processRequest(fromDataSetName, toDataSetName, true);
        }

        // copy currDataSet(member) to .
        // copy dataset(member) to .
        if (".".equals(secondParam)) {
            if (currDataSet.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }
            if (datasetMemberFirstParam != null) {
                return processRequest(fromDataSetName,
                        currDataSet + "(" + datasetMemberFirstParam.getMember() + ")", false);
            }
            if (DsnUtil.isMember(firstParam)) {
                return processRequest(currDataSet + "(" + firstParam + ")",
                        currDataSet + "(" + firstParam + ")", false);
            }
            if (DsnUtil.isDataSet(firstParam)) {
                return new ResponseStatus(Constants.COPY_NO_MEMBER_ERROR, false);
            }
        }

        // copy dataset(member) to currDataSet(member)
        if (datasetMemberFirstParam != null && DsnUtil.isMember(secondParam)) {
            return processRequest(fromDataSetName, currDataSet + "(" + secondParam + ")", false);
        }

        long numOfAsterisk = firstParam.chars().filter(ch -> ch == '*').count();
        boolean copyWildCard = numOfAsterisk == 1 && DsnUtil.isMember(firstParam.substring(0, firstParam.indexOf("*")));

        // copy member wild card to dataset
        if (copyWildCard) {
            List<Member> members;
            try {
                members = new MemberLst(new DsnList(connection), timeout).memberLst(currDataSet);
            } catch (ZosmfRequestException e) {
                final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
                return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
            }

            toDataSetName = secondParam;
            if (!DsnUtil.isDataSet(toDataSetName)) {
                return new ResponseStatus("specify valid dataset destination, try again...", false);
            }

            // target is a member string without * (wild card)
            final var target = firstParam.substring(0, firstParam.indexOf("*"));
            members = DsnUtil.getMembersByStartsWithFilter(target, members);
            if (members.size() == 1) {
                final var name = members.get(0).getMember().orElse("");
                fromDataSetName = currDataSet + "(" + name + ")";
                return processRequest(fromDataSetName, toDataSetName, false);
            }

            if (members.isEmpty()) {
                return new ResponseStatus(Constants.COPY_NOTHING_WARNING, false);
            }

            final var futures = new ArrayList<Future<ResponseStatus>>();
            final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
            for (final var member : members) {
                final var name = member.getMember().orElse("");
                fromDataSetName = currDataSet + "(" + name + ")";
                toDataSetName += "(" + name + ")";
                final var dsnCopy = new DsnCopy(connection);
                final var future = new FutureCopy(dsnCopy, fromDataSetName, toDataSetName, false);
                futures.add(pool.submit(future));
            }
            return FutureUtil.getFutureResponses(futures, pool, timeout,
                    toDataSetName.length() + Constants.STRING_PAD_LENGTH);
        }

        return new ResponseStatus(Constants.INVALID_ARGUMENTS, false);
    }

    private ResponseStatus processRequest(final String source, final String destination, boolean isCopyAll) {
        LOG.debug("*** processResult ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var dsnCopy = new DsnCopy(connection);
        final var futureCopy = new FutureCopy(dsnCopy, source, destination, isCopyAll);
        final var submit = pool.submit(futureCopy);
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
