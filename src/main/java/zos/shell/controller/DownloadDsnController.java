package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.configuration.ConfigSingleton;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.DownloadDsnService;
import zos.shell.utility.ResponseUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadDsnController {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadDsnController.class);

    private final DownloadDsnService downloadDsnService;

    public DownloadDsnController(final DownloadDsnService downloadDsnService) {
        LOG.debug("*** DownloadDsnController ***");
        this.downloadDsnService = downloadDsnService;
    }

    public List<String> download(final String dataset, final String target) {
        LOG.debug("*** download ***");
        List<String> results = new ArrayList<>();
        var errMsg = "cannot download " + target + ", try again...";

        // TODO incorporate env variable downloadPath availability too..
        var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        if (configSettings.getDownloadPath().isBlank()) {
            results.add("downloadPath configuration missing");
            results.add(errMsg);
            return results;
        }
        File dir = new File(configSettings.getDownloadPath());
        if (!dir.isDirectory()) {
            results.add("downloadPath setting " + configSettings.getDownloadPath() + " does not exist");
            results.add(errMsg);
            return results;
        }

        List<ResponseStatus> responseStatus = downloadDsnService.download(dataset, target);
        if (responseStatus.size() > 1) {
            responseStatus.forEach(r -> results.add(r.getMessage()));
            return results;
        } else if (responseStatus.size() == 1 && responseStatus.get(0).isStatus()) {
            responseStatus.forEach(r -> results.add(ResponseUtil.getMsgAfterArrow(r.getMessage())));
            return results;
        } else {
            responseStatus.forEach(r -> results.add(ResponseUtil.getMsgAfterArrow(r.getMessage())));
            results.add(errMsg);
        }
        return results;
    }

}

