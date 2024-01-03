package zos.shell.future;

import zos.shell.response.ResponseStatus;
import zos.shell.service.job.PurgeCmd;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZosConnection;

import java.util.concurrent.Callable;

public class FuturePurgeJob extends PurgeCmd implements Callable<ResponseStatus> {

    private enum JobIdIdentifier {

        JOB("JOB"),
        TSU("TSU"),
        STC("STC");

        private final String value;

        JobIdIdentifier(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    private final String item;

    public FuturePurgeJob(ZosConnection connection, String item) {
        super(connection);
        this.item = item;
    }

    @Override
    public ResponseStatus call() {
        if ((item.startsWith(JobIdIdentifier.JOB.getValue()) ||
                item.startsWith(JobIdIdentifier.TSU.getValue()) ||
                item.startsWith(JobIdIdentifier.STC.getValue()))) {

            final var id = item.substring(3);
            if (Util.isStrNum(id)) {
                return this.purgeJobByJobId(item);
            }
        }
        return this.purgeJobByJobName(item);
    }

}