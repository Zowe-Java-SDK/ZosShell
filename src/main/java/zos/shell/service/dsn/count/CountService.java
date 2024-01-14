package zos.shell.service.dsn.count;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.datasetlst.DatasetListingService;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.utility.DsnUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CountService {

    private static final Logger LOG = LoggerFactory.getLogger(CountService.class);

    private final DsnList dsnList;
    private final long timeout;

    public CountService(final DsnList dsnList, final long timeout) {
        LOG.debug("*** CountService ***");
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public ResponseStatus count(final String dataset, final String filter) {
        LOG.debug("*** count ***");
        if (!DsnUtil.isDataSet(dataset)) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }
        final var dataSetCount = new AtomicInteger();
        List<Dataset> datasets = new ArrayList<>();
        List<Member> members = new ArrayList<>();

        if ("members".equalsIgnoreCase(filter)) {
            final var memberLst = new MemberListingService(dsnList, timeout);
            try {
                members = memberLst.memberLst(dataset);
            } catch (ZosmfRequestException e) {
                return new ResponseStatus(e.getMessage(), false);
            }
        }

        if ("datasets".equalsIgnoreCase(filter)) {
            final var datasetLst = new DatasetListingService(dsnList, timeout);
            try {
                datasets = datasetLst.datasetLst(dataset);
            } catch (ZosmfRequestException e) {
                return new ResponseStatus(e.getMessage(), false);
            }
        }

        datasets.forEach(item -> {
            if (!item.getDsname().orElse("n\\a").equalsIgnoreCase(dataset)) {
                dataSetCount.getAndIncrement();
            }
        });

        return new ResponseStatus(String.valueOf(members.size() + dataSetCount.get()), true);
    }

}
