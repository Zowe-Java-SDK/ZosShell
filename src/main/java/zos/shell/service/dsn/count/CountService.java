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
import zowe.client.sdk.zosfiles.dsn.model.Dataset;
import zowe.client.sdk.zosfiles.dsn.model.Member;

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
        if (!DsnUtil.isMember(dataset) && !DsnUtil.isDataset(dataset)) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }
        var dataSetCount = new AtomicInteger();
        List<Dataset> datasets = new ArrayList<>();
        List<Member> members = new ArrayList<>();

        if ("members".equalsIgnoreCase(filter)) {
            var memberListingService = new MemberListingService(dsnList, timeout);
            try {
                members = memberListingService.memberLst(dataset);
            } catch (ZosmfRequestException e) {
                return new ResponseStatus(e.getMessage(), false);
            }
        }

        if ("datasets".equalsIgnoreCase(filter)) {
            var datasetListingService = new DatasetListingService(dsnList, timeout);
            try {
                datasets = datasetListingService.datasetLst(dataset);
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
