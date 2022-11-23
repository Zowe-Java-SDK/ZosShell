package zos.shell.future;

import zos.shell.commands.Download;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.ZosDsnDownload;

import java.util.concurrent.Callable;

public class FutureDownload extends Download implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureDownload(ZosDsnDownload download, String dataSet, String member, boolean isBinary) {
        super(download, isBinary);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.download(dataSet, member);
    }

}
