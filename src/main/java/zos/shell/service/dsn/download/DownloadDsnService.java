package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DataSetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FileUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DownloadDsnService {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadDsnService.class);

    private final ZosConnection connection;
    private final boolean isBinary;
    private final long timeout;

    public DownloadDsnService(final ZosConnection connection, boolean isBinary, final long timeout) {
        LOG.debug("*** DownloadDsnService ***");
        this.connection = connection;
        this.isBinary = isBinary;
        this.timeout = timeout;
    }

    public List<ResponseStatus> download(final String dataset, String target) {
        LOG.debug("*** download ***");
        List<ResponseStatus> results = new ArrayList<>();
        List<Member> members;

        // dataset is current dataset
        // target can be either a member in current dataset or a sequential dataset or dataset(member) notation

        // download all members in dataset
        if ("*".equals(target)) {
            try {
                members = new MemberListingService(new DsnList(connection), timeout).memberLst(dataset);
            } catch (ZosmfRequestException e) {
                var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
                return List.of(new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false));
            }
            if (members.isEmpty()) {
                results.add(new ResponseStatus(Constants.DOWNLOAD_NOTHING_WARNING, false));
            }
            results.addAll(downloadMembers(dataset, members));
            return results;
        }

        // download all members that filter by member wild card in dataset
        if (target.contains("*") && DsnUtil.isMember(target.substring(0, target.indexOf("*")))) {
            try {
                members = new MemberListingService(new DsnList(connection), timeout).memberLst(dataset);
            } catch (ZosmfRequestException e) {
                var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
                return List.of(new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false));
            }
            // transform target is a member string without * (wild card)
            target = target.substring(0, target.indexOf("*"));
            members = DsnUtil.getMembersByStartsWithFilter(target, members);
            if (members.isEmpty()) {
                results.add(new ResponseStatus(Constants.DOWNLOAD_NOTHING_WARNING, false));
            }
            results.addAll(downloadMembers(dataset, members));
            return results;
        }

        var dataSetMember = DataSetMember.getDatasetAndMember(target);
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = null;
        try {
            if (dataSetMember != null) {
                // dataset(member) notation
                submit = pool.submit(new FutureMemberDownload(new DsnGet(connection), dataSetMember.getDataset(),
                        dataSetMember.getMember(), isBinary));
                results.add(submit.get(timeout, TimeUnit.SECONDS));
            } else if (DsnUtil.isMember(target)) {
                // member in current dataset
                submit = pool.submit(new FutureMemberDownload(new DsnGet(connection), dataset, target, isBinary));
                results.add(submit.get(timeout, TimeUnit.SECONDS));
            } else if (DsnUtil.isDataSet(target)) {
                // sequential dataset
                submit = pool.submit(new FutureDatasetDownload(new DsnGet(connection), target, isBinary));
                results.add(submit.get(timeout, TimeUnit.SECONDS));
            } else {
                results.add(new ResponseStatus(Constants.INVALID_DATASET_AND_MEMBER, false));
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
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

    private List<ResponseStatus> downloadMembers(final String dataset, final List<Member> members) {
        LOG.debug("*** downloadMembers ***");
        List<ResponseStatus> results = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        var futures = new ArrayList<Future<ResponseStatus>>();

        for (var member : members) {
            if (member.getMember().isPresent()) {
                String name = member.getMember().get();
                futures.add(pool.submit(new FutureMemberDownload(new DsnGet(connection), dataset, name, isBinary)));
            }
        }

        for (var future : futures) {
            try {
                results.add(future.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException e) {
                LOG.debug("error: " + e);
                future.cancel(true);
                results.add(new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                        e.getMessage() : Constants.EXECUTE_ERROR_MSG, false));
            } catch (TimeoutException e) {
                future.cancel(true);
                results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
            }
        }

        // results.get(0) not possible if we used FutureUtil.getFutureResponses
        var file = new File(results.get(0).getOptionalData());
        FileUtil.openFileLocation(file.getAbsolutePath());
        pool.shutdownNow();
        return results;
    }

}