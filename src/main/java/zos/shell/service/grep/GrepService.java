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
import zowe.client.sdk.zosfiles.dsn.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GrepService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GrepService.class);

    private final ZosConnection connection;
    private final PathService pathService;
    private final String pattern;
    private final long timeout;
    private final ExecutorService poolMax = Executors.newFixedThreadPool(Constants.THREAD_POOL_MAX);
    private final ExecutorService poolMin = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public GrepService(final ZosConnection connection,
                       final PathService pathService,
                       final String pattern,
                       final long timeout) {
        LOG.debug("*** GrepService ***");
        this.connection = connection;
        this.pathService = pathService;
        this.pattern = pattern;
        this.timeout = timeout;
    }

    public List<String> search(final String dataset, final String target) {
        LOG.debug("*** search ***");
        List<String> result = new ArrayList<>();

        boolean isWildCardOnly = "*".equals(target);
        boolean isMemberWildCard = isMemberWildcard(target);

        if (!isWildCardOnly && !isMemberWildCard) {
            return searchSingleTarget(dataset, target, result);
        }

        List<Member> members = loadMembers(dataset, result);
        if (members.isEmpty()) {
            return result;
        }

        if (isWildCardOnly) {
            return futureResults(dataset, result, members);
        }

        // Since isMemberWildcard(target) already guarantees one trailing *, use -1 to extract filter value
        String filter = target.substring(0, target.length() - 1).toUpperCase();
        List<Member> filteredMembers = members.stream()
                .filter(m -> m != null
                        && m.getMember() != null
                        && !m.getMember().isBlank()
                        && m.getMember().startsWith(filter))
                .collect(Collectors.toList());

        return futureResults(dataset, result, filteredMembers);
    }

    private List<Member> loadMembers(final String dataset, final List<String> result) {
        LOG.debug("*** loadMembers ***");
        try (var memberListingService = new MemberListingService(new DsnList(connection), timeout)) {
            List<Member> members = memberListingService.listMembers(dataset);
            if (members.isEmpty()) {
                result.add("nothing found, try again...");
            }
            return members;
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            result.add(errMsg != null ? errMsg : e.getMessage());
            return List.of();
        }
    }

    private List<String> searchSingleTarget(final String dataset,
                                            final String target,
                                            final List<String> result) {
        LOG.debug("*** searchSingleTarget ***");
        DsnGet dsnGet = new DsnGet(this.connection);
        Download download = new Download(dsnGet, this.pathService, false);
        ConcatService concatService = new ConcatService(download, this.timeout);

        Future<List<String>> future = poolMin.submit(new FutureGrep(
                concatService,
                dataset,
                target,
                this.pattern,
                false
        ));

        try {
            result.addAll(future.get(this.timeout, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            result.add(getErrorMessage(Constants.COMMAND_EXECUTION_ERROR_MSG, e));
        } catch (ExecutionException e) {
            future.cancel(true);
            result.add(getErrorMessage(Constants.COMMAND_EXECUTION_ERROR_MSG, e));
        } catch (TimeoutException e) {
            future.cancel(true);
            result.add(Constants.TIMEOUT_MESSAGE);
        }

        return result;
    }

    private boolean isMemberWildcard(final String target) {
        LOG.debug("*** isMemberWildcard ***");
        long count = target.chars().filter(ch -> ch == '*').count();
        return count == 1 && target.endsWith("*");
    }

    private List<String> futureResults(final String dataset,
                                       final List<String> result,
                                       final List<Member> members) {
        LOG.debug("*** futureResults ***");
        List<Future<List<String>>> futures = new ArrayList<>();

        for (Member member : members) {
            if (member != null && member.getMember() != null && !member.getMember().isBlank()) {
                String name = member.getMember();
                ConcatService concatService = createConcatServiceForMemberSearch();
                futures.add(poolMax.submit(new FutureGrep(
                        concatService,
                        dataset,
                        name,
                        this.pattern,
                        true
                )));
            }
        }

        for (var future : futures) {
            try {
                result.addAll(future.get(this.timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException e) {
                future.cancel(true);
                result.add(getErrorMessage(Constants.EXECUTE_ERROR_MSG, e));
            } catch (TimeoutException e) {
                future.cancel(true);
                result.add(Constants.TIMEOUT_MESSAGE);
            }
        }

        return result;
    }

    // Create per-task service chain to avoid sharing non-thread-safe state across grep tasks.
    private ConcatService createConcatServiceForMemberSearch() {
        LOG.debug("*** createConcatServiceForMemberSearch ***");
        DsnGet dsnGet = new DsnGet(this.connection);
        EnvVariableService envVarService = new EnvVariableService();
        EnvVariableController envVarController = new EnvVariableController(envVarService);
        PathService memberPathService = new PathService(ConnSingleton.getInstance(), envVarController);
        Download download = new Download(dsnGet, memberPathService, true);
        return new ConcatService(download, this.timeout);
    }

    @Override
    public void close() {
        poolMax.shutdown();
        poolMin.shutdown();
    }

    private static String getErrorMessage(final String msg, final Exception e) {
        LOG.debug("*** getErrorMessage ***");
        return e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : msg;
    }

}
