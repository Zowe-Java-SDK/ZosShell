package zos.shell.future;

import zowe.client.sdk.zosjobs.input.JobFile;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureBrowseJob implements Callable<StringBuilder> {

    private final JobGet jobGet;
    private final List<JobFile> files;

    public FutureBrowseJob(JobGet jobGet, List<JobFile> files) {
        this.jobGet = jobGet;
        this.files = files;
    }

    @Override
    public StringBuilder call() {
        final var str = new StringBuilder();
        files.forEach(file -> {
            try {
                str.append(List.of(jobGet.getSpoolContent(file)));
            } catch (Exception ignored) {
            }
        });
        return str;
    }

}
