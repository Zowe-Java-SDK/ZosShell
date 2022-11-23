package zos.shell.future;

import zos.shell.commands.PurgeJob;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.core.ZOSConnection;

import java.util.concurrent.Callable;

public class FuturePurgeJob extends PurgeJob implements Callable<ResponseStatus> {

    private final String job;

    public FuturePurgeJob(ZOSConnection connection, String job) {
        super(connection);
        this.job = job;
    }

    @Override
    public ResponseStatus call() throws Exception {
        return this.purgeJobByJobName(job);
    }

}
