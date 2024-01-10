package zos.shell.record;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.utility.DsnUtil;

public class DataSetMember {

    private static final Logger LOG = LoggerFactory.getLogger(DataSetMember.class);

    private final String dataset;
    private final String member;

    public DataSetMember(String dataset, String member) {
        this.dataset = dataset;
        this.member = member;
    }

    public String getDataset() {
        return dataset;
    }

    public String getMember() {
        return member;
    }

    public static DataSetMember getDatasetAndMember(String target) {
        LOG.debug("*** getMemberFromDataSet ***");
        final var index = target.indexOf("(");
        if (index == -1) {
            return null;
        }
        final var dataset = target.substring(0, index);
        if (!DsnUtil.isDataSet(dataset)) {
            return null;
        }

        final var member = target.substring(index + 1, target.length() - 1);
        if (!DsnUtil.isMember(member)) {
            return null;
        }
        return new DataSetMember(dataset, member);
    }

    @Override
    public String toString() {
        return "DataSetMember{" +
                "dataSet='" + dataset + '\'' +
                ", member='" + member + '\'' +
                '}';
    }

}
