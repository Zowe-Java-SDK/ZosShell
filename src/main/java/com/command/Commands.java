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
        IssueCommand issueCommand = new IssueCommand(connection);
        IssueParams params = new IssueParams();
        params.setCommand("C " + param);
        ConsoleResponse response;
        try {
            response = issueCommand.issue(params);
            String result = response.getCommandResponse().get();
            // remove last newline i.e. \n
            terminal.printf(result.substring(0, result.length() - 1) + "\n");
        } catch (Exception e) {
        }
    }

    public void cat(ZOSConnection connection, String dataSet, String param) {
        ZosDsnDownload dl = new ZosDsnDownload(connection);
        DownloadParams dlParams = new DownloadParams.Builder().build();
        InputStream inputStream;
        try {
            if (Util.isDataSet(param)) {
                inputStream = dl.downloadDsn(String.format("%s", param), dlParams);
            } else {
                inputStream = dl.downloadDsn(String.format("%s(%s)", dataSet, param), dlParams);
            }
            display(inputStream);
        } catch (Exception e) {
        }
    }

    public String cd(String currDataSet, String param) {
        if (Util.isDataSet(param)) {
            return param;
        } else if (param.equals("..") && !currDataSet.isEmpty()) {
            String[] tokens = currDataSet.split("\\.");
            int length = tokens.length - 1;
            if (length == 1) {
                terminal.printf("cant change to high qualifier level, try again...\n");
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
            terminal.printf("invalid dataset or cant change to high qualifier level, try again...\n");
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

    public void copy(ZOSConnection connection, List<String> members, String currDataSet, String[] params) {
        final ZosDsnCopy zosDsnCopy = new ZosDsnCopy(connection);
        final String memberOrDataset = params[1].toUpperCase();
        final String dataset = params[2];
        String member = "";
        String fromDataSetName;
        String toDataSetName;
        boolean copyAllMembers = false;

        if (members.isEmpty() && !currDataSet.isEmpty()) {
            final ZosDsnList zosDsnList = new ZosDsnList(connection);
            ListParams listParams = new ListParams.Builder().build();
            try {
                members = zosDsnList.listDsnMembers(currDataSet, listParams);
            } catch (Exception e) {
            }
        }

        if (members.isEmpty())
            return;

        if (!Util.isDataSet(memberOrDataset)) {
            member = memberOrDataset;
            if (!members.contains(member) && !".".equals(member)) {
                terminal.printf("member does not exist, try again.." + "\n");
                return;
            }
            fromDataSetName = currDataSet + "(" + member + ")";
        } else {
            fromDataSetName = memberOrDataset;
        }

        toDataSetName = dataset;
        if (Util.isDataSet(dataset) && !member.isEmpty()) {
            toDataSetName += "(" + member + ")";
        }

        if (".".equals(member)) {
            fromDataSetName = currDataSet;
            toDataSetName = dataset;
            copyAllMembers = true;
        }

        try {
            zosDsnCopy.copy(fromDataSetName, toDataSetName, true, copyAllMembers);
        } catch (Exception e) {
            terminal.printf(e.getMessage() + "\n");
        }

    }

    public void count(ZOSConnection connection, String dataSet, String param) {
        ZosDsnList zosDsnList = new ZosDsnList(connection);
        ListParams params = new ListParams.Builder().build();
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
        ZosDsnList zosDsnList = new ZosDsnList(connection);
        ListParams params = new ListParams.Builder().build();
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
        }
        return members;
    }

    public List<String> lsl(ZOSConnection connection, String dataSet) {
        ZosDsnList zosDsnList = new ZosDsnList(connection);
        ListParams params = new ListParams.Builder().build();
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
        }
        return members;
    }

    public void ps(ZOSConnection connection) {
        ps(connection, null);
    }

    public void ps(ZOSConnection connection, String task) {
        GetJobs getJobs = new GetJobs(connection);
        List<Job> jobs = null;
        try {
            GetJobParams.Builder getJobParams = new GetJobParams.Builder("*");
            if (task != null) {
                getJobParams.prefix(task).build();
            }
            GetJobParams params = getJobParams.build();
            jobs = getJobs.getJobsCommon(params);
        } catch (Exception e) {
        }
        jobs.sort(Comparator.comparing((Job j) -> j.getJobName().get())
                .thenComparing(j -> j.getStatus().get()).thenComparing(j -> j.getJobId().get()));
        jobs.forEach(job -> terminal.printf(
                String.format("%-8s %-8s %-8s\n", job.getJobName().get(), job.getJobId().get(), job.getStatus().get()))
        );
    }

    public void submit(ZOSConnection connection, String dataSet, String param) {
        SubmitJobs submitJobs = new SubmitJobs(connection);
        Job job = null;
        try {
            job = submitJobs.submitJob(String.format("%s(%s)", dataSet, param));
        } catch (Exception e) {
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

}
