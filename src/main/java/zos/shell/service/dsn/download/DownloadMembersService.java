package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.model.Member;

import java.util.ArrayList;
import java.util.List;

public class DownloadMembersService {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadMembersService.class);

    private final ZosConnection connection;
    private final DownloadMemberListService downloadMembersService;
    private final long timeout;

    public DownloadMembersService(final ZosConnection connection,
                                  final DownloadMemberListService downloadMembersService,
                                  final long timeout) {
        LOG.debug("*** DownloadMembersService ***");
        this.connection = connection;
        this.downloadMembersService = downloadMembersService;
        this.timeout = timeout;
    }

    public List<ResponseStatus> downloadMembers(final String dataset, final String prefix) {
        LOG.debug("Downloading members from dataset '{}' with prefix '{}'", dataset, prefix);
        return downloadMembersCommon(dataset, prefix);
    }

    public List<ResponseStatus> downloadMembers(final String dataset) {
        LOG.debug("Downloading all members for dataset: {}", dataset);
        return downloadMembersCommon(dataset, null);
    }

    private List<ResponseStatus> downloadMembersCommon(final String dataset, final String prefix) {
        LOG.debug("*** downloadMembersCommon ***");

        List<Member> members;
        try (var memberListingService = new MemberListingService(new DsnList(connection), timeout)) {
            members = memberListingService.listMembers(dataset);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return List.of(new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false));
        }

        if (members.isEmpty()) {
            return List.of(new ResponseStatus(Constants.DOWNLOAD_NOTHING_WARNING, false));
        }

        List<Member> result = members;
        if (prefix != null && !prefix.isEmpty()) {
            result = DsnUtil.getMembersByStartsWithFilter(prefix, members);
            if (result.isEmpty()) {
                return List.of(new ResponseStatus(Constants.DOWNLOAD_NOTHING_WARNING, false));
            }
        }
        return new ArrayList<>(downloadMembersService.downloadMembers(dataset, result));
    }

}
