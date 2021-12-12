package com.data;

public class DataSetMember {

    private String dataSet;
    private String member;

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
