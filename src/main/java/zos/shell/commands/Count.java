package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.input.ListParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Count {

    private static final Logger LOG = LoggerFactory.getLogger(Count.class);

    private final DsnList dsnList;
    private final ListParams params = new ListParams.Builder().build();

    public Count(DsnList dsnList) {
        LOG.debug("*** Count ***");
        this.dsnList = dsnList;
    }

    public ResponseStatus count(String dataSet, String param) {
        LOG.debug("*** count ***");
        final var dataSetCount = new AtomicInteger();
        List<Dataset> ds = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        try {
            if ("members".equalsIgnoreCase(param)) {
                members = dsnList.getMembers(dataSet, params);
            }
            if ("datasets".equalsIgnoreCase(param)) {
                ds = dsnList.getDatasets(dataSet, params);
            }
            ds.forEach(item -> {
                if (!item.getDsname().orElse("n\\a").equalsIgnoreCase(dataSet)) {
                    dataSetCount.getAndIncrement();
                }
            });
        } catch (Exception e) {
            return new ResponseStatus("0", false);
        }
        return new ResponseStatus(String.valueOf(members.size() + dataSetCount.get()), true);
    }

}
