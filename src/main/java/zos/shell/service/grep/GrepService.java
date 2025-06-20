package zos.shell.service.grep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.controller.EnvVariableController;
import zos.shell.service.dsn.concat.ConcatService;
import zos.shell.service.dsn.download.Download;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.service.path.PathService;
import zos.shell.singleton.ConnSingleton;
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

public class GrepService {

    private static final Logger LOG = LoggerFactory.getLogger(GrepService.class);

    private final ZosConnection connection;
    private final PathService pathService;
    private final String pattern;
    private final long timeout;

    public GrepService(final ZosConnection connection, final PathService pathService, String pattern, long timeout) {
        LOG.debug("*** GrepService ***");
        this.connection = connection;
        this.pathService = pathService;
        this.pattern = pattern;
        this.timeout = timeout;
    }

    public List<String> search(String dataset, String target) {
        LOG.debug("*** search ***");
        List<String> result = new ArrayList<>();
        ExecutorService pool = null;
        var futures = new ArrayList<Future<List<String>>>();

        long count = target.chars().filter(ch -> ch == '*').count();
        boolean endsWithWildCard = target.endsWith("*");
        boolean memberWildCard = count == 1 && endsWithWildCard;
        boolean wildCardOnly = "*".equals(target);

        List<Member> members = new ArrayList<>();
        if (memberWildCard || wildCardOnly) {
            var memberListingService = new MemberListingService(new DsnList(connection), timeout);

            try {
                members = memberListingService.memberLst(dataset);
            } catch (ZosmfRequestException e) {
                var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
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
            var value = target.substring(0, target.indexOf("*")).toUpperCase();
            members = members.stream()
                    .filter(m -> m.getMember().isPresent() && m.getMember().get().startsWith(value))
                    .collect(Collectors.toList());

            return futureResults(dataset, result, pool, futures, members);
        } else {
            pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
            var concatService = new ConcatService(
                    new Download(new DsnGet(connection), pathService, false), timeout);
            Future<List<String>> submit = pool.submit(
                    new FutureGrep(concatService, dataset, target, pattern, false));

            try {
                result.addAll(submit.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException e) {
                LOG.debug("exception error: {}", String.valueOf(e));
                submit.cancel(true);
                result.add(e.getMessage() != null && !e.getMessage().isBlank() ?
                        e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG);
            } catch (TimeoutException e) {
                submit.cancel(true);
                result.add(Constants.TIMEOUT_MESSAGE);
            } finally {
                pool.shutdown();
            }

            return result;
        }
    }

    private List<String> futureResults(final String dataset, final List<String> result, final ExecutorService pool,
                                       final ArrayList<Future<List<String>>> futures, final List<Member> members) {
        for (var member : members) {
            var concatService = new ConcatService(new Download(new DsnGet(connection),
                    new PathService(ConnSingleton.getInstance(),
                            new EnvVariableController(new EnvVariableService())), false), timeout);
            if (member.getMember().isPresent()) {
                var name = member.getMember().get();
                futures.add(pool.submit(new FutureGrep(concatService, dataset, name, pattern, true)));
            }
        }

        for (var future : futures) {
            try {
                result.addAll(future.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException e) {
                LOG.debug("exception error: {}", String.valueOf(e));
                future.cancel(true);
                result.add(e.getMessage() != null && !e.getMessage().isBlank() ?
                        e.getMessage() : Constants.EXECUTE_ERROR_MSG);
            } catch (TimeoutException e) {
                future.cancel(true);
                result.add(Constants.TIMEOUT_MESSAGE);
            }
        }

        pool.shutdownNow();
        return result;
    }

}

