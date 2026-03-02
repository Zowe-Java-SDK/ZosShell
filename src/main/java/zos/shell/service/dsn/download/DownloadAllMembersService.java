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
import zowe.client.sdk.zosfiles.dsn.model.Member;

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
        LOG.debug("Downloading all members for target: {}", target);

        final List<Member> members;
        try {
            DsnList dsnList = new DsnList(connection);
            members = new MemberListingService(dsnList, timeout).memberLst(target);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return List.of(new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false));
        }

        if (members.isEmpty()) {
            return List.of(new ResponseStatus(Constants.DOWNLOAD_NOTHING_WARNING, false));
        }

        return new ArrayList<>(downloadMembersService.downloadMembers(target, members));
    }

}
