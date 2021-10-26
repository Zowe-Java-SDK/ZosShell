package com;

import core.ZOSConnection;
import org.apache.commons.io.IOUtils;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.beryx.textio.web.RunnerData;
import utility.UtilIO;
import zosconsole.ConsoleResponse;
import zosconsole.IssueCommand;
import zosconsole.input.IssueParams;
import zosfiles.ZosDsnDownload;
import zosfiles.ZosDsnList;
import zosfiles.input.DownloadParams;
import zosfiles.input.ListParams;
import zosjobs.GetJobs;
import zosjobs.SubmitJobs;
import zosjobs.input.GetJobParams;
import zosjobs.response.Job;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZosShell implements BiConsumer<TextIO, RunnerData> {

    private static final String TOO_MANY_PARAMETERS = "too many parameters, try again...\n";

    private static List<String> dataSets = new ArrayList<>();
    private static String currDataSet = "";
    private static final String hostName = "xxxx";
    private static final String zosmfPort = "xxxx";
    private static final String userName = "xxxx";
    private static final String password = "xxxx";
    private static ZOSConnection connection = new ZOSConnection(hostName, zosmfPort, userName, password);
    private static TextTerminal<?> terminal;

    public static void main(String[] args) {
        SwingTextTerminal mainTerm = new SwingTextTerminal();
        mainTerm.setPaneTitle("ZosShell");
        mainTerm.init();
        TextIO mainTextIO = new TextIO(mainTerm);
        new ZosShell().accept(mainTextIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        terminal = textIO.getTextTerminal();
        terminal.println("Connected to " + hostName + " with user " + userName);

        String[] commands;
        String commandLine = "";
        while (!"end".equalsIgnoreCase(commandLine)) {
            commandLine = textIO.newStringInputReader().withMaxLength(80).read(">");
            System.out.print("> ");
            commands = commandLine.split(" ");
            doCommand(commands);
        }

        textIO.dispose();
    }

    private static void doCommand(String[] commands) {
        String command = commands[0];
        String param;

        switch (command.toLowerCase()) {
            case "cancel":
                if (commands.length == 1)
                    return;
                if (isParamsExceeded(2, commands))
                    return;
                param = commands[1];
                cancel(param);
                break;
            case "cat":
                if (commands.length == 1)
                    return;
                if (isParamsExceeded(2, commands))
                    return;
                param = commands[1];
                cat(param);
                break;
            case "cd":
                if (commands.length == 1)
                    return;
                if (isParamsExceeded(2, commands))
                    return;
                if (cd(commands[1])) return;
                dataSets.add(currDataSet);
                break;
            case "count":
                if (commands.length == 1) {
                    terminal.printf("specified either \"count members\" or \"count datasets\"\n");
                    return;
                }
                if (!("members".equalsIgnoreCase(commands[1]) || "datasets".equalsIgnoreCase(commands[1]))) {
                    terminal.printf("specified either \"count members\" or \"count datasets\"\n");
                    return;
                }
                if (isParamsExceeded(2, commands))
                    return;
                param = commands[1];
                count(param);
                break;
            case "end":
                break;
            case "ls":
                if (isParamsExceeded(2, commands))
                    return;
                if (commands.length == 2 && !"-l".equalsIgnoreCase(commands[1])) {
                    terminal.printf(TOO_MANY_PARAMETERS);
                    return;
                }
                if (commands.length == 2 && "-l".equalsIgnoreCase(commands[1])) {
                    lsl();
                    return;
                }
                if (currDataSet.isEmpty())
                    return;
                ls();
                break;
            case "ps":
                if (isParamsExceeded(2, commands))
                    return;
                if (commands.length > 1) {
                    ps(commands[1]);
                } else {
                    ps();
                }
                break;
            case "pwd":
                if (isParamsExceeded(1, commands))
                    return;
                if (currDataSet.isEmpty())
                    return;
                terminal.printf(currDataSet + "\n");
                break;
            case "submit":
                if (commands.length == 1)
                    return;
                if (isParamsExceeded(2, commands))
                    return;
                if (currDataSet.isEmpty())
                    return;
                param = commands[1];
                submit(param);
                break;
            case "visited":
                if (isParamsExceeded(1, commands))
                    return;
                if (currDataSet.isEmpty())
                    return;
                dataSets.forEach(terminal::println);
                break;
            default:
                terminal.printf("invalid command, try again...\n");
        }
    }

    private static void cancel(String param) {
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

    private static void cat(String param) {
        ZosDsnDownload dl = new ZosDsnDownload(connection);
        DownloadParams dlParams = new DownloadParams.Builder().build();
        InputStream inputStream;
        try {
            if (isDataSet(param)) {
                inputStream = dl.downloadDsn(String.format("%s", param), dlParams);
            } else {
                inputStream = dl.downloadDsn(String.format("%s(%s)", currDataSet, param), dlParams);
            }
            display(inputStream);
        } catch (Exception e) {
        }
    }

    private static boolean cd(String command1) {
        String dataSet = command1;

        if (isDataSet(dataSet)) {
            currDataSet = dataSet;
        } else if (dataSet.equals("..") && !currDataSet.isEmpty()) {

            String[] tokens = currDataSet.split("\\.");
            int length = tokens.length - 1;
            if (length == 1) {
                terminal.printf("cant change to high qualifier level, try again...\n");
                return true;
            }

            StringBuilder newDataSet = new StringBuilder();
            for (int i = 0; i < length; i++) {
                newDataSet.append(tokens[i]);
                newDataSet.append(".");
            }

            String str = newDataSet.toString();
            str = str.substring(0, str.length() - 1);
            currDataSet = str;
        } else {
            terminal.printf("invalid dataset or cant change to high qualifier level, try again...\n");
            return true;
        }
        return false;
    }

    private static void count(String param) {
        ZosDsnList zosDsnList = new ZosDsnList(connection);
        ListParams params = new ListParams.Builder().build();
        List<String> items = new ArrayList<>();
        try {
            if ("members".equalsIgnoreCase(param)) {
                items = zosDsnList.listDsnMembers(currDataSet, params);
            }
            if ("datasets".equalsIgnoreCase(param)) {
                items = zosDsnList.listDsn(currDataSet, params);
            }
        } catch (Exception e) {
            terminal.printf("0" + "\n");
            return;
        }
        terminal.printf(items.size() + "\n");
    }

    private static void display(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, UtilIO.UTF8);
            String[] content = writer.toString().split("\\n");
            Arrays.stream(content).forEach(c -> terminal.println(c));
        }
        inputStream.close();
    }

    private static boolean isDataSet(String dataSetName) {
        dataSetName = dataSetName.toUpperCase(Locale.ROOT);
        String invalidDatasetMsg = "Invalid data set name '" + dataSetName + "'.";

        // Check that the dataset contains more than one segment
        // This could be valid for additionalTests
        String[] segments = dataSetName.split("\\.");
        if (segments.length < 2) {
            return false;
        }

        // The length cannot be longer than 44
        if (dataSetName.length() > 44) {
            return false;
        }

        // The name cannot contain two successive periods
        if (dataSetName.contains("..")) {
            return false;
        }

        // Cannot end in a period
        if (dataSetName.endsWith(".")) {
            return false;
        }

        // Each segment cannot be more than 8 characters
        // Each segment's first letter is a letter or #, @, $.
        // The remaining seven characters in a segment can be letters, numbers, and #, @, $, -
        for (String segment : segments) {
            if (segment.length() > 8) {
                return false;
            }
            Pattern p = Pattern.compile("[A-Z#@\\$]{1}[A-Z0-9#@\\$\\-]{1,7}");
            Matcher m = p.matcher(segment);
            if (!m.matches()) {
                return false;
            }
        }

        return true;
    }

    private static boolean isParamsExceeded(int num, String[] commands) {
        if (commands.length > num) {
            terminal.printf(TOO_MANY_PARAMETERS);
            return true;
        }
        return false;
    }

    private static void ls() {
        ZosDsnList zosDsnList = new ZosDsnList(connection);
        ListParams params = new ListParams.Builder().build();
        try {
            List<String> dataSetNames = zosDsnList.listDsn(currDataSet, params);
            dataSetNames.forEach(ds -> {
                if (!ds.equalsIgnoreCase(currDataSet))
                    terminal.printf(ds + "\n");
            });
            List<String> members = zosDsnList.listDsnMembers(currDataSet, params);
            members.forEach(m -> terminal.printf(m + "\n"));
        } catch (Exception e) {
        }
    }

    private static void lsl() {
        ZosDsnList zosDsnList = new ZosDsnList(connection);
        ListParams params = new ListParams.Builder().build();
        try {
            List<String> dataSetNames = zosDsnList.listDsn(currDataSet, params);
            dataSetNames.forEach(ds -> {
                if (!ds.equalsIgnoreCase(currDataSet))
                    terminal.printf(ds + "\n");
            });
            List<String> members = zosDsnList.listDsnMembers(currDataSet, params);
            int size = members.size();
            int threeColumns = 3;
            int fourColumns = 4;
            int fiveColumns = 5;
            int sixColumns = 6;
            int columns = 0;

            if (size > 5)
                columns = threeColumns;
            if (size > 20)
                columns = fourColumns;
            if (size > 40)
                columns = fiveColumns;
            if (size > 60)
                columns = sixColumns;

            int numOfLines = size / columns;
            String[] lines = new String[numOfLines + 1];

            if (size > 5) {
                int lineIndex = 0;
                for (int i = 0; i < size; ) {
                    int count = 1;
                    String line = "";
                    while (count % (columns + 1) != 0) {
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
    }

    private static void ps() {
        ps(null);
    }

    private static void ps(String task) {
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
            e.printStackTrace();
        }
        jobs.sort(Comparator.comparing((Job j) -> j.getJobName().get())
                .thenComparing(j -> j.getStatus().get()).thenComparing(j -> j.getJobId().get()));
        jobs.forEach(job -> terminal.printf(
                String.format("%-8s %-8s %-8s\n", job.getJobName().get(), job.getJobId().get(), job.getStatus().get()))
        );
    }

    private static void submit(String param) {
        SubmitJobs submitJobs = new SubmitJobs(connection);
        Job job = null;
        try {
            job = submitJobs.submitJob(String.format("%s(%s)", currDataSet, param));
        } catch (Exception e) {
        }
        if (job != null)
            terminal.printf("Job Name: " + job.getJobName().orElse("n\\a") +
                    ", Job Id: " + job.getJobId().orElse("n\\a") + "\n");
    }

}
