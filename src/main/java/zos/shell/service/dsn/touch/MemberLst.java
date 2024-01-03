package zos.shell.service.dsn.touch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MemberLst {

    private static final Logger LOG = LoggerFactory.getLogger(MemberLst.class);

    private final DsnList dsnList;
    private final long timeout;

    public MemberLst(final DsnList dsnList, long timeout) {
        LOG.debug("*** MemberLst ***");
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public boolean memberExist(final String dataset, final String member) throws ZosmfRequestException {
        LOG.debug("*** memberExist ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureMemberLst(dsnList, dataset));

        List<Member> members;
        try {
            members = submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ZosmfRequestException(e.getMessage());
        }

        if (members.isEmpty()) {
            return false;
        }

        return members.stream().anyMatch(m -> m.getMember().isPresent() && m.getMember().get().equalsIgnoreCase(member));
    }

}
