package zos.shell.commands;

import com.google.common.base.Strings;
import zos.shell.Constants;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.DirectorySetup;
import zos.shell.utility.Util;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.Collections;
import java.util.Comparator;

public class DownloadJob {

    private final BrowseJob browseJob;

    public DownloadJob(GetJobs getJobs, boolean isAll, long timeOutValue) {
        this.browseJob = new BrowseJob(getJobs, isAll, timeOutValue);
    }

    public ResponseStatus download(String name) {
        if (!Util.isMember(name)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }

        final var error = "error retrieving " + name + " log ";
        final var responseStatus = browseJob.browseJob(name);
        if (!responseStatus.isStatus() && responseStatus.getMessage().contains("timeout")) {
            return new ResponseStatus(Constants.BROWSE_TIMEOUT, false);
        } else if (!responseStatus.isStatus()) {
            return new ResponseStatus(responseStatus.getMessage(), false);
        }

        final var output = responseStatus.getMessage();
        Comparator<Job> comparator = Comparator.comparing(o -> o.getJobId().get());
        browseJob.jobs.sort(comparator);
        Collections.reverse(browseJob.jobs);
        final var id = browseJob.jobs.get(0).getJobId().orElse(null);

        final var dirSetup = new DirectorySetup();
        try {
            dirSetup.initialize(name, id);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseStatus(error, false);
        }

        try {
            Util.writeTextFile(output, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseStatus(error, false);
        }

        final var message = Strings.padStart(name, 8, ' ') + Constants.ARROW
                + "downloaded to " + dirSetup.getFileNamePath();
        return new ResponseStatus(message, true, dirSetup.getFileNamePath());
    }

}