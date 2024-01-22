package zos.shell.record;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.utility.DsnUtil;

public class DatasetMember {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetMember.class);

    private final String dataset;
    private final String member;

    public DatasetMember(final String dataset, final String member) {
        LOG.debug("*** DatasetMember ***");
        this.dataset = dataset;
        this.member = member;
    }

    public String getDataset() {
        LOG.debug("*** getDataset ***");
        return dataset;
    }

    public String getMember() {
        LOG.debug("*** getMember ***");
        return member;
    }

    public static DatasetMember getDatasetAndMember(final String target) {
        LOG.debug("*** getMemberFromDataSet ***");
        int index = target.indexOf("(");
        if (index == -1) {
            return null;
        }
        var dataset = target.substring(0, index);
        if (!DsnUtil.isDataset(dataset)) {
            return null;
        }

        var member = target.substring(index + 1, target.length() - 1);
        if (!DsnUtil.isMember(member)) {
            return null;
        }
        return new DatasetMember(dataset, member);
    }

    @Override
    public String toString() {
        return "DataSetMember{" +
                "dataSet='" + dataset + '\'' +
                ", member='" + member + '\'' +
                '}';
    }

}
