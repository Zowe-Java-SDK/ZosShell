package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;

public class DownloadAllMembersService {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadAllMembersService.class);

    private final ZosConnection connection;
    private final DownloadMemberListService downloadMembersService;
    private final long timeout;


    public DownloadAllMembersService(final ZosConnection connection,
                                     final DownloadMemberListService downloadMembersService,
                                     final long timeout) {
        LOG.debug("*** DownloadAllMembersService ***");
        this.connection = connection;
        this.downloadMembersService = downloadMembersService;
        this.timeout = timeout;
    }

    public List<ResponseStatus> downloadAllMembers(final String target) {
        LOG.debug("*** downloadAllMembers ***");
        List<ResponseStatus> results = new ArrayList<>();
        List<Member> members;

        try {
            members = new MemberListingService(new DsnList(connection), timeout).memberLst(target);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return List.of(new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false));
        }
        if (members.isEmpty()) {
            results.add(new ResponseStatus(Constants.DOWNLOAD_NOTHING_WARNING, false));
        }
        results.addAll(downloadMembersService.downloadMembers(target, members));
        return results;
    }

}
