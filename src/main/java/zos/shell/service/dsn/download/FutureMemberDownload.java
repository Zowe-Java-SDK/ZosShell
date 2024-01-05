package zos.shell.service.dsn.download;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.util.concurrent.Callable;

public class FutureMemberDownload extends Download implements Callable<ResponseStatus> {

    private final String dataset;
    private final String member;

    public FutureMemberDownload(final DsnGet download, final String dataset, final String member, boolean isBinary) {
        super(download, isBinary);
        this.dataset = dataset;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.member(dataset, member);
    }

}
