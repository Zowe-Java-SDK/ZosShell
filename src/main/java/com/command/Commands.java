package com.command;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.apache.commons.io.IOUtils;
import org.beryx.textio.TextTerminal;
import utility.UtilIO;
import zosconsole.ConsoleResponse;
import zosconsole.IssueCommand;
import zosconsole.input.IssueParams;
import zosfiles.ZosDsn;
import zosfiles.ZosDsnCopy;
import zosfiles.ZosDsnDownload;
import zosfiles.ZosDsnList;
import zosfiles.input.DownloadParams;
import zosfiles.input.ListParams;
import zosfiles.response.Dataset;
import zosjobs.GetJobs;
import zosjobs.SubmitJobs;
import zosjobs.input.GetJobParams;
import zosjobs.response.Job;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Commands {

    private final List<ZOSConnection> connections;
    private final TextTerminal<?> terminal;

    public Commands(List<ZOSConnection> connections, TextTerminal<?> terminal) {
        this.connections = connections;
        this.terminal = terminal;
    }

    public void cancel(ZOSConnection connection, String param) {
        final IssueCommand issueCommand = new IssueCommand(connection);
        final IssueParams params = new IssueParams();
        params.setCommand("C " + param);
        ConsoleResponse response;
        try {
            response = issueCommand.issue(params);
            String result = response.getCommandResponse().get();
            // remove last newline i.e. \n
            terminal.printf(result.substring(0, result.length() - 1) + "\n");
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    public void cat(ZOSConnection connection, String dataSet, String param) {
        final ZosDsnDownload dl = new ZosDsnDownload(connection);
        final DownloadParams dlParams = new DownloadParams.Builder().build();
        InputStream inputStream;
        try {
            if (Util.isDataSet(param)) {
                inputStream = dl.downloadDsn(String.format("%s", param), dlParams);
            } else {
                inputStream = dl.downloadDsn(String.format("%s(%s)", dataSet, param), dlParams);
            }
            display(inputStream);
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    public String cd(ZOSConnection connection, String currDataSet, String param) {
        if (Util.isDataSet(param)) {
            return param;
        } else if (param.equals("..") && !currDataSet.isEmpty()) {
            String[] tokens = currDataSet.split("\\.");
            final int length = tokens.length - 1;
            if (length == 1) {
                terminal.printf(Constants.HIGH_QUALIFIER_ERROR + "\n");
                return currDataSet;
            }

            StringBuilder str = new StringBuilder();
            for (int i = 0; i < length; i++) {
                str.append(tokens[i]);
                str.append(".");
            }

            String dataSet = str.toString();
            dataSet = dataSet.substring(0, str.length() - 1);
            return dataSet;
        } else {
            final String dataSetName = param;
            final ZosDsnList zosDsnList = new ZosDsnList(connection);
            final ListParams params = new ListParams.Builder().build();
            List<Dataset> dsLst = new ArrayList<>();
            try {
                dsLst = zosDsnList.listDsn(currDataSet, params);
            } catch (Exception e) {
                if (e.getMessage().contains("Connection refused")) {
                    terminal.printf(Constants.SEVERE_ERROR + "\n");
                    return currDataSet;
                }
            }
            String findDataSet = currDataSet + "." + dataSetName;
            boolean found = dsLst.stream().anyMatch(d -> d.getDsname().get().contains(findDataSet));
            if (found)
                currDataSet += "." + dataSetName;
            else terminal.printf(Constants.DATASET_OR_HIGH_QUALIFIER_ERROR + "\n");
            return currDataSet;
        }
    }

    public ZOSConnection change(ZOSConnection connection, String[] commands) {
        int index = Integer.parseInt(commands[1]);
        if (index-- > connections.size()) {
            terminal.printf(Constants.NO_CONNECTION + "\n");
            return connection;
        }
        return connections.get(index);
    }

    public void connections(ZOSConnection connection) {
        if (connection != null) {
            AtomicInteger i = new AtomicInteger(1);
            connections.forEach(c ->
                    terminal.printf(i.getAndIncrement() + " " + "hostname: " + c.getHost() + ", port: " +
                            c.getZosmfPort() + ", user = " + c.getUser() + "\n")
            );
        } else {
            terminal.printf(Constants.NO_CONNECTION_INFO + "\n");
        }
    }

    public void copy(ZOSConnection connection, String currDataSet, String[] params) {
        final ZosDsnCopy zosDsnCopy = new ZosDsnCopy(connection);

        String fromDataSetName = "";
        String toDataSetName = "";
        boolean copyAllMembers = false;

        String param1 = params[1].toUpperCase();
        String param2 = params[2].toUpperCase();

        if (Util.isMember(param1)) {
            fromDataSetName = currDataSet + "(" + param1 + ")";
        }

        if (Util.isMember(param2)) {
            toDataSetName = currDataSet + "(" + param2 + ")";
        }

        if (".".equals(param1) && ".".equals(param2)) {
            terminal.printf(Constants.INVALID_COMMAND + "\n");
            return;
        }

        if (".".equals(param1)) {
            fromDataSetName = currDataSet;
            if (Util.isDataSet(param2))
                toDataSetName = param2;
            else {
                terminal.printf("second argument invalid for copy all operation, try again...\n");
                return;
            }
            copyAllMembers = true;
        }

        if (".".equals(param2)) {

            if (Util.isMember(param1)) {
                terminal.printf(Constants.COPY_OPS_ITSELF_ERROR + "\n");
                return;
            }

            if (Util.isDataSet(param1)) {
                terminal.printf(Constants.COPY_OPS_NO_MEMBER_ERROR + "\n");
                return;
            }

            if (param1.contains(currDataSet)) {
                terminal.printf(Constants.COPY_OPS_ITSELF_ERROR + "\n");
                return;
            }

            if (param1.contains("(") && param1.contains(")")) {
                String member;
                String dataset;

                int index = param1.indexOf("(");
                dataset = param1.substring(0, index);
                if (!Util.isDataSet(dataset)) {
                    terminal.printf(Constants.COPY_OPS_NO_MEMBER_AND_DATASET_ERROR + "\n");
                    return;
                }

                member = param1.substring(index + 1, param1.length() - 1);
                fromDataSetName = param1;
                toDataSetName = currDataSet + "(" + member + ")";
            }

        }

        if (Util.isMember(param1) && Util.isDataSet(param2)) {
            fromDataSetName = currDataSet + "(" + param1 + ")";
            toDataSetName = param2 + "(" + param1 + ")";
        }

        if (fromDataSetName.isEmpty())
            fromDataSetName = param1;

        if (toDataSetName.isEmpty())
            toDataSetName = param2;

        try {
            zosDsnCopy.copy(fromDataSetName, toDataSetName, true, copyAllMembers);
        } catch (Exception e) {
            printError(e.getMessage());
        }

    }

    public void count(ZOSConnection connection, String dataSet, String param) {
        final ZosDsnList zosDsnList = new ZosDsnList(connection);
        final ListParams params = new ListParams.Builder().build();
        List<Dataset> ds = new ArrayList<>();
        List<String> members = new ArrayList<>();
        try {
            if ("members".equalsIgnoreCase(param)) {
                members = zosDsnList.listDsnMembers(dataSet, params);
            }
            if ("datasets".equalsIgnoreCase(param)) {
                ds = zosDsnList.listDsn(dataSet, params);
            }
        } catch (Exception e) {
            terminal.printf("0" + "\n");
            return;
        }
        terminal.printf(members.size() + ds.size() + "\n");
    }

    public List<String> ls(ZOSConnection connection, String dataSet) {
        final ZosDsnList zosDsnList = new ZosDsnList(connection);
        final ListParams params = new ListParams.Builder().build();
        List<String> members = new ArrayList<>();
        try {
            List<Dataset> dataSets = zosDsnList.listDsn(dataSet, params);
            dataSets.forEach(ds -> {
                if (!ds.getDsname().get().equalsIgnoreCase(dataSet))
                    terminal.printf(ds.getDsname().get() + "\n");
            });
            members = zosDsnList.listDsnMembers(dataSet, params);
            members.forEach(m -> terminal.printf(m + "\n"));
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused"))
                terminal.printf(Constants.SEVERE_ERROR + "\n");
        }
        return members;
    }

    public List<String> lsl(ZOSConnection connection, String dataSet) {
        final ZosDsnList zosDsnList = new ZosDsnList(connection);
        final ListParams params = new ListParams.Builder().build();
        List<String> members = new ArrayList<>();
        try {
            List<Dataset> dataSets = zosDsnList.listDsn(dataSet, params);
            dataSets.forEach(ds -> {
                if (!ds.getDsname().get().equalsIgnoreCase(dataSet))
                    terminal.printf(ds.getDsname().get() + "\n");
            });
            members = zosDsnList.listDsnMembers(dataSet, params);
            int size = members.size();
            int numOfColumns = 0;

            if (size > 1 && size < 100)
                numOfColumns = 3;
            else if (size > 100 && size < 300) {
                numOfColumns = 4;
            } else if (size > 300 && size < 500) {
                numOfColumns = 5;
            } else if (size > 500 && size < 700) {
                numOfColumns = 6;
            } else if (size > 700 && size < 900) {
                numOfColumns = 7;
            } else if (size >= 1000)
                numOfColumns = 8;

            int numOfLines = size / numOfColumns;
            String[] lines = new String[numOfLines + 1];

            if (size > 5) {
                int lineIndex = 0;
                for (int i = 0; i < size; ) {
                    int count = 1;
                    String line = "";
                    while (count % (numOfColumns + 1) != 0) {
                        if (i >= size) break;
                        line += String.format("%-8s", members.get(i++)) + " ";
                        count++;
                    }
                    lines[lineIndex++] = line;
                }
            }

            Arrays.stream(lines).forEach(line -> {
                if (line != null) terminal.printf(line + "\n");
            });
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused"))
                terminal.printf(Constants.SEVERE_ERROR + "\n");
        }
        return members;
    }

    public void ps(ZOSConnection connection) {
        ps(connection, null);
    }

    public void ps(ZOSConnection connection, String task) {
        final GetJobs getJobs = new GetJobs(connection);
        List<Job> jobs;
        try {
            GetJobParams.Builder getJobParams = new GetJobParams.Builder("*");
            if (task != null) {
                getJobParams.prefix(task).build();
            }
            GetJobParams params = getJobParams.build();
            jobs = getJobs.getJobsCommon(params);
        } catch (Exception e) {
            printError(e.getMessage());
            return;
        }
        jobs.sort(Comparator.comparing((Job j) -> j.getJobName().get())
                .thenComparing(j -> j.getStatus().get()).thenComparing(j -> j.getJobId().get()));
        jobs.forEach(job -> terminal.printf(
                String.format("%-8s %-8s %-8s\n", job.getJobName().get(), job.getJobId().get(), job.getStatus().get()))
        );
    }

    public void rm(ZOSConnection connection, String currDataSet, String param) {
        ZosDsn zosDsn = new ZosDsn(connection);

        if ("*".equals(param)) {
            if (currDataSet.isEmpty()) {
                terminal.printf("nothing to delete, try again..\n");
                return;
            }
            final ZosDsnList zosDsnList = new ZosDsnList(connection);
            final ListParams params = new ListParams.Builder().build();
            List<String> members = new ArrayList<>();
            try {
                members = zosDsnList.listDsnMembers(currDataSet, params);
            } catch (Exception e) {
                printError(e.getMessage());
            }
            members.forEach(m -> {
                try {
                    zosDsn.deleteDsn(currDataSet, m);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (Util.isMember(param)) {
            if (currDataSet.isEmpty()) {
                terminal.printf("nothing to delete, try again..\n");
                return;
            }
            try {
                zosDsn.deleteDsn(currDataSet, param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void submit(ZOSConnection connection, String dataSet, String param) {
        final SubmitJobs submitJobs = new SubmitJobs(connection);
        Job job;
        try {
            job = submitJobs.submitJob(String.format("%s(%s)", dataSet, param));
        } catch (Exception e) {
            printError(e.getMessage());
            return;
        }
        if (job != null)
            terminal.printf("Job Name: " + job.getJobName().orElse("n\\a") +
                    ", Job Id: " + job.getJobId().orElse("n\\a") + "\n");
    }

    private void display(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, UtilIO.UTF8);
            String[] content = writer.toString().split("\\n");
            Arrays.stream(content).forEach(c -> terminal.println(c));
        }
        inputStream.close();
    }

    private void printError(String message) {
        if (message.contains("Not Found")) {
            terminal.printf(Constants.NOT_FOUND + "\n");
        } else if (message.contains("Connection refused")) {
            terminal.printf(Constants.SEVERE_ERROR + "\n");
        } else if (message.contains("dataSetName not specified")) {
            terminal.printf(Constants.NO_DATASET + "\n");
        } else {
            terminal.printf(message + "\n");
        }
    }

}
