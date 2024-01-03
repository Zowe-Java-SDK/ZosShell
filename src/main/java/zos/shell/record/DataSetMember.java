package zos.shell.record;

public class DataSetMember {

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

    @Override
    public String toString() {
        return "DataSetMember{" +
                "dataSet='" + dataSet + '\'' +
                ", member='" + member + '\'' +
                '}';
    }

}
