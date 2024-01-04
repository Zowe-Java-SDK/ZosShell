package zos.shell.service.job.browse;

import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosjobs.input.JobFile;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureBrowse implements Callable<StringBuilder> {

    private final JobGet retrieve;
    private final List<JobFile> files;

    public FutureBrowse(final JobGet retrieve, final List<JobFile> files) {
        this.retrieve = retrieve;
        this.files = files;
    }

    @Override
    public StringBuilder call() {
        final var str = new StringBuilder();
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
