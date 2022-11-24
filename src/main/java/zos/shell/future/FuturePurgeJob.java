package zos.shell.future;

import zos.shell.commands.PurgeJob;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZOSConnection;

import java.util.concurrent.Callable;

public class FuturePurgeJob extends PurgeJob implements Callable<ResponseStatus> {

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

    public FuturePurgeJob(ZOSConnection connection, String item) {
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