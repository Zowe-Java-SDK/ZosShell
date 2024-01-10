package zos.shell.service.job.purge;

import zos.shell.response.ResponseStatus;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.zosjobs.methods.JobDelete;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FuturePurge extends Purge implements Callable<ResponseStatus> {

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

    private final String filter;

    public FuturePurge(final JobDelete delete, final JobGet retrieve, final String filter) {
        super(delete, retrieve);
        this.filter = filter;
    }

    @Override
    public ResponseStatus call() {
        if ((filter.startsWith(JobIdIdentifier.JOB.getValue()) ||
                filter.startsWith(JobIdIdentifier.TSU.getValue()) ||
                filter.startsWith(JobIdIdentifier.STC.getValue()))) {

            final var id = filter.substring(3);
            if (StrUtil.isStrNum(id)) {
                return this.purgeById(filter);
            }
        }
        return this.purgeByName(filter);
    }

}