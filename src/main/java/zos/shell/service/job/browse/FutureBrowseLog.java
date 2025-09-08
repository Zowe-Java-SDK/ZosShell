package zos.shell.service.job.browse;

import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.model.JobFile;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureBrowseLog implements Callable<StringBuilder> {

    private final JobGet retrieve;
    private final List<JobFile> files;

    public FutureBrowseLog(final JobGet retrieve, final List<JobFile> files) {
        this.retrieve = retrieve;
        this.files = files;
    }

    @Override
    public StringBuilder call() {
        var str = new StringBuilder();
        files.forEach(file -> {
            try {
                str.append(List.of(retrieve.getSpoolContent(file)
                        .replaceAll("[\r\n]+", "\n")
                        .replaceAll("[\n\n]+", "\n")));
            } catch (ZosmfRequestException ignored) {
            }
        });
        return str;
    }

}
