package zos.shell.future;

import zos.shell.service.dsn.DownloadCmd;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.util.concurrent.Callable;

public class FutureDownload extends DownloadCmd implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureDownload(DsnGet download, String dataSet, String member, boolean isBinary) {
        super(download, isBinary);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.download(dataSet, member);
    }

}
