package zos.shell.service.memberlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.model.Member;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MemberListingService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MemberListingService.class);

    private final DsnList dsnList;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public MemberListingService(final DsnList dsnList, long timeout) {
        LOG.debug("*** MemberListingService ***");
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public boolean memberExists(final String dataset, final String member) throws ZosmfRequestException {
        LOG.debug("*** memberExists ***");
        return getMembers(dataset).stream()
                .map(Member::getMember)
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .anyMatch(name -> name.equalsIgnoreCase(member));
    }

    public List<Member> listMembers(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** listMembers ***");
        return this.getMembers(dataset);
    }

    private List<Member> getMembers(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** getMembers ***");
        Future<List<Member>> future = pool.submit(new FutureMemberListing(
                dsnList,
                dataset,
                timeout
        ));
        return FutureUtil.getFutureValue(future, timeout);
    }

    @Override
    public void close() {
        LOG.debug("*** close ***");
        pool.shutdown();
    }

}
