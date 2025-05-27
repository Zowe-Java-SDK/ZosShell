package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.*;
import zos.shell.singleton.configuration.ConfigSingleton;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class DownloadDsnController {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadDsnController.class);

    private final DownloadMemberService downloadMemberService;
    private final DownloadPdsMemberService downloadPdsMemberService;
    private final DownloadSeqDatasetService downloadSeqDatasetService;
    private final DownloadAllMembersService downloadAllMembersService;
    private final DownloadMembersService downloadMembersService;

    public DownloadDsnController(final DownloadMemberService downloadMemberService,
                                 final DownloadPdsMemberService downloadPdsMemberService,
                                 final DownloadSeqDatasetService downloadSeqDatasetService,
                                 final DownloadAllMembersService downloadAllMembersService,
                                 final DownloadMembersService downloadMembersService) {

        LOG.debug("*** DownloadDsnController ***");
        this.downloadMemberService = downloadMemberService;
        this.downloadPdsMemberService = downloadPdsMemberService;
        this.downloadSeqDatasetService = downloadSeqDatasetService;
        this.downloadAllMembersService = downloadAllMembersService;
        this.downloadMembersService = downloadMembersService;
    }

    public List<String> download(final String dataset, final String target) {
        LOG.debug("*** download ***");
        List<String> results = new ArrayList<>();

        // TODO incorporate env variable downloadPath availability too..
        var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        if (configSettings.getDownloadPath().isBlank()) {
            results.add("downloadPath configuration missing, try again...");
            return results;
        }
        File dir = new File(configSettings.getDownloadPath());
        if (!dir.isDirectory()) {
            results.add("downloadPath " + configSettings.getDownloadPath() + " not found, try again...");
            return results;
        }

        var dataSetMember = DatasetMember.getDatasetAndMember(target);
        boolean found = false;
        List<ResponseStatus> responseStatus = List.of();
        if (dataSetMember != null) {
            found = true;
            responseStatus = downloadPdsMemberService.downloadPdsMember(dataSetMember);
        } else if (DsnUtil.isMember(target)) {
            if (dataset.isBlank()) {
                return warning(results);
            }
            found = true;
            responseStatus = downloadMemberService.downloadMember(dataset, target);
        } else if (DsnUtil.isDataset(target)) {
            found = true;
            responseStatus = downloadSeqDatasetService.downloadSeqDataset(target);
        } else if ("*".equals(target)) {
            if (dataset.isBlank()) {
                return warning(results);
            }
            found = true;
            responseStatus = downloadAllMembersService.downloadAllMembers(dataset);
        } else if (target.contains("*")) {
            IntStream charCodes = target.chars();
            OptionalInt last = charCodes.reduce((first, second) -> second);
            if (last.isPresent() && last.getAsInt() == '*' &&
                    DsnUtil.isMember(target.substring(0, target.indexOf("*")))) {
                if (dataset.isBlank()) {
                    return warning(results);
                }
                found = true;
                responseStatus = downloadMembersService.downloadMembers(dataset,
                        target.substring(0, target.indexOf("*")));
            }
        }

        if (!found) {
            results.add(Constants.INVALID_DATASET_AND_MEMBER_COMBINED);
            return results;
        }

        if (responseStatus.size() > 1) {
            responseStatus.forEach(r -> results.add(r.getMessage()));
            return results;
        } else if (responseStatus.size() == 1 && responseStatus.get(0).isStatus()) {
            responseStatus.forEach(r -> results.add(ResponseUtil.getMsgAfterArrow(r.getMessage())));
            return results;
        } else {
            responseStatus.forEach(r -> results.add(ResponseUtil.getMsgAfterArrow(r.getMessage())));
        }
        return results;
    }

    private static List<String> warning(List<String> results) {
        results.add("PWD empty; cd to PDS(E), try again...");
        return results;
    }


}

