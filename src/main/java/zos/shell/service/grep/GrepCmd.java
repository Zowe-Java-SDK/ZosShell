package zos.shell.service.grep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.dsn.download.DownloadCmd;
import zos.shell.service.dsn.concatenate.ConcatCmd;
import zos.shell.service.memberlst.MemberLst;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GrepCmd {

    private static final Logger LOG = LoggerFactory.getLogger(GrepCmd.class);

    private final ZosConnection connection;
    private final String pattern;
    private final long timeout;

    public GrepCmd(final ZosConnection connection, String pattern, long timeout) {
        LOG.debug("*** GrepCmd ***");
        this.connection = connection;
        this.pattern = pattern;
        this.timeout = timeout;
    }

    public List<String> search(String dataset, String target) {
        LOG.debug("*** search ***");
        List<String> result = new ArrayList<>();
        ExecutorService pool = null;
        final var futures = new ArrayList<Future<List<String>>>();

        long count = target.chars().filter(ch -> ch == '*').count();
        boolean endsWithWildCard = target.endsWith("*");
        boolean memberWildCard = count == 1 && endsWithWildCard;
        boolean wildCardOnly = "*".equals(target);

        List<Member> members = new ArrayList<>();
        if (memberWildCard || wildCardOnly) {
            final var memberLst = new MemberLst(new DsnList(connection), timeout);

            try {
                members = memberLst.memberLst(dataset);
            } catch (ZosmfRequestException e) {
                final var errMsg = Util.getResponsePhrase(e.getResponse());
                result.add(errMsg != null ? errMsg : e.getMessage());
                return result;
            }

            if (members.isEmpty()) {
                result.add("nothing found, try again...");
                return result;
            }

            pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
        }

        if (wildCardOnly) {
            return futureResults(dataset, result, pool, futures, members);
        } else if (memberWildCard) {
            final var value = target.substring(0, target.indexOf("*")).toUpperCase();
            members = members.stream()
                             .filter(m -> m.getMember().isPresent() && m.getMember().get().startsWith(value))
                             .collect(Collectors.toList());

            return futureResults(dataset, result, pool, futures, members);
        } else {
            pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
            final var concatCmd = new ConcatCmd(new DownloadCmd(new DsnGet(connection), false), timeout);
            final var submit = pool.submit(new FutureGrep(concatCmd, dataset, target, pattern, false));

            try {
                result.addAll(submit.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                result.add(Constants.TIMEOUT_MESSAGE);
                LOG.debug("error: " + e);
            }

            pool.shutdownNow();
            return result;
        }
    }

    private List<String> futureResults(final String dataset, final List<String> result, final ExecutorService pool,
                                       final ArrayList<Future<List<String>>> futures, final List<Member> members) {
        for (final var member : members) {
            final var concatCmd = new ConcatCmd(new DownloadCmd(new DsnGet(connection), false), timeout);
            if (member.getMember().isPresent()) {
                final var name = member.getMember().get();
                futures.add(pool.submit(new FutureGrep(concatCmd, dataset, name, pattern, true)));
            }
        }

        for (final var future : futures) {
            try {
                result.addAll(future.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                result.add(Constants.TIMEOUT_MESSAGE);
                LOG.debug("error: " + e);
            }
        }

        pool.shutdownNow();
        return result;
    }

}

