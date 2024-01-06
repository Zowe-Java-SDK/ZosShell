package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DataSetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberLst;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FileUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DownloadDsnCmd {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadDsnCmd.class);

    public static final String DIRECTORY_PATH_WINDOWS = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\";
    public static final String DIRECTORY_PATH_MAC = Constants.PATH_FILE_DIRECTORY_MAC + "/";
    private final ZosConnection connection;
    private final boolean isBinary;
    private final long timeout;

    public DownloadDsnCmd(final ZosConnection connection, boolean isBinary, final long timeout) {
        LOG.debug("*** DownloadCmd ***");
        this.connection = connection;
        this.isBinary = isBinary;
        this.timeout = timeout;
    }

    public List<ResponseStatus> download(final String dataset, final String target) {
        LOG.debug("*** download ***");
        List<ResponseStatus> results = new ArrayList<>();
        List<Member> members = new ArrayList<>();

        // dataset is current dataset
        // target can be either a member in current dataset or a sequential dataset or dataset(member) notation

        // download all members in dataset
        if ("*".equals(target)) {

            final var memberLst = new MemberLst(new DsnList(connection), timeout);
            try {
                members = memberLst.memberLst(dataset);
            } catch (ZosmfRequestException e) {
                results.add(new ResponseStatus(e.getMessage(), false));
            }

            if (members.isEmpty()) {
                results.add(new ResponseStatus(Constants.DOWNLOAD_NOTHING_WARNING, false));
            }

            results.addAll(downloadMembers(dataset, members, isBinary));
            return results;
        }

        // download all members that filter by member wild card in dataset
        if (target.contains("*") && DsnUtil.isMember(target.substring(0, target.indexOf("*")))) {

            final var memberLst = new MemberLst(new DsnList(connection), timeout);
            try {
                members = memberLst.memberLst(dataset);
            } catch (ZosmfRequestException e) {
                results.add(new ResponseStatus(e.getMessage(), false));
            }

            final var index = target.indexOf("*");
            final var searchForMember = target.substring(0, index).toUpperCase();
            members = members.stream()
                    .filter(i -> i.getMember().isPresent() && i.getMember().get().startsWith(searchForMember))
                    .collect(Collectors.toList());

            if (members.isEmpty()) {
                results.add(new ResponseStatus(Constants.DOWNLOAD_NOTHING_WARNING, false));
            }

            results.addAll(downloadMembers(dataset, members, isBinary));
            return results;
        }

        final var dataSetMember = DataSetMember.getDatasetAndMember(target);
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = null;
        try {
            if (dataSetMember != null) {
                // dataset(member) notation
                submit = pool.submit(new FutureMemberDownload(new DsnGet(connection), dataSetMember.getDataSet(),
                        dataSetMember.getMember(), isBinary));
                results.add(submit.get(timeout, TimeUnit.SECONDS));
            } else if (DsnUtil.isMember(target)) {
                // member in current dataset
                submit = pool.submit(new FutureMemberDownload(new DsnGet(connection), dataset, target, isBinary));
                results.add(submit.get(timeout, TimeUnit.SECONDS));
            } else {
                // sequential dataset
                submit = pool.submit(new FutureDatasetDownload(new DsnGet(connection), target, isBinary));
                results.add(submit.get(timeout, TimeUnit.SECONDS));
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
            FileUtil.openFileLocation(results.get(0).getOptionalData());
            return results;
        } else {
            results.add(0, new ResponseStatus(ResponseUtil.getMsgAfterArrow(results.get(0).getMessage()), false));
        }

        return results;
    }

    private List<ResponseStatus> downloadMembers(final String dataset, final List<Member> members, boolean isBinary) {
        LOG.debug("*** downloadMembers ***");
        final var results = new ArrayList<ResponseStatus>();
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        final var futures = new ArrayList<Future<ResponseStatus>>();

        for (final var member : members) {
            if (member.getMember().isPresent()) {
                final var name = member.getMember().get();
                futures.add(pool.submit(new FutureMemberDownload(new DsnGet(connection), dataset, name, isBinary)));
            }
        }

        for (final var future : futures) {
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

        FileUtil.openFileLocation(results.get(0).getOptionalData());
        pool.shutdownNow();
        return results;
    }

}