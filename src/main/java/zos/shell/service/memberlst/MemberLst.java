package zos.shell.service.memberlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.List;
import java.util.concurrent.*;

public class MemberLst {

    private static final Logger LOG = LoggerFactory.getLogger(MemberLst.class);

    private final DsnList dsnList;
    private final long timeout;

    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public MemberLst(final DsnList dsnList, long timeout) {
        LOG.debug("*** MemberLst ***");
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public boolean memberExist(final String dataset, final String member) throws ZosmfRequestException {
        LOG.debug("*** memberExist ***");
        final var submit = pool.submit(new FutureMemberLst(dsnList, dataset, timeout));

        List<Member> members;
        try {
            members = submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
            submit.cancel(true);
            throw new ZosmfRequestException(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG);
        } catch (TimeoutException e) {
            submit.cancel(true);
            throw new ZosmfRequestException(Constants.TIMEOUT_MESSAGE);
        } finally {
            pool.shutdown();
        }

        if (members.isEmpty()) {
            return false;
        }

        return members.stream().anyMatch(m -> m.getMember().isPresent() && m.getMember().get().equalsIgnoreCase(member));
    }

    public List<Member> memberLst(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** memberLst ***");
        final var submit = pool.submit(new FutureMemberLst(dsnList, dataset, timeout));

        List<Member> members;
        try {
            members = submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
            submit.cancel(true);
            throw new ZosmfRequestException(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_INVALID_COMMAND);
        } catch (TimeoutException e) {
            submit.cancel(true);
            throw new ZosmfRequestException(Constants.TIMEOUT_MESSAGE);
        } finally {
            pool.shutdown();
        }

        return members;
    }

}
