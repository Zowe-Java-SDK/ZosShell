package zos.shell.record;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.utility.Util;

public class DataSetMember {

    private static final Logger LOG = LoggerFactory.getLogger(DataSetMember.class);

    private final String dataSet;
    private final String member;

    public DataSetMember(String dataSet, String member) {
        this.dataSet = dataSet;
        this.member = member;
    }

    public String getDataSet() {
        return dataSet;
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
        if (!Util.isDataSet(dataset)) {
            return null;
        }

        final var member = target.substring(index + 1, target.length() - 1);
        if (!Util.isMember(member)) {
            return null;
        }
        return new DataSetMember(dataset, member);
    }

    @Override
    public String toString() {
        return "DataSetMember{" +
                "dataSet='" + dataSet + '\'' +
                ", member='" + member + '\'' +
                '}';
    }

}
