package com.commands;

import com.Constants;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosjobs.GetJobs;

import java.util.Arrays;
import java.util.List;

public class Tail extends JobLog {

    public Tail(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll, long timeOutValue) {
        super(terminal, getJobs, isAll, timeOutValue);
    }

    public StringBuilder tail(String[] params) {
        StringBuilder result;
        try {
            result = browseJobLog(params[1]);
        } catch (Exception e) {
            if (e.getMessage().contains("timeout")) {
                terminal.println(Constants.BROWSE_TIMEOUT);
                return null;
            }
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return null;
            }
            Util.printError(terminal, e.getMessage());
            return null;
        }
        List<String> output = Arrays.asList(result.toString().split("\n"));

        final var size = output.size();
        var lines = 0;
        if (params.length == 3) {
            if (!"all".equalsIgnoreCase(params[2])) {
                try {
                    lines = Integer.parseInt(params[2]);
                } catch (NumberFormatException e) {
                    terminal.println(Constants.INVALID_PARAMETER);
                    return null;
                }
            }
        }
        if (params.length == 4) {
            try {
                lines = Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
        }

        if (lines > 0) {
            if (lines < size) {
                return display(lines, size, output);
            } else {
                return displayAll(output);
            }
        } else {
            int LINES_LIMIT = 25;
            if (size > LINES_LIMIT) {
                return display(LINES_LIMIT, size, output);
            } else {
                return displayAll(output);
            }
        }
    }

    private StringBuilder displayAll(List<String> output) {
        final var stringBuilder = new StringBuilder();
        output.forEach(line -> {
            terminal.println(line);
            stringBuilder.append(line).append("\n");
        });
        return stringBuilder;
    }

    private StringBuilder display(int lines, int size, List<String> output) {
        final var stringBuilder = new StringBuilder();
        for (var i = size - lines; i < size; i++) {
            terminal.println(output.get(i));
            stringBuilder.append(output.get(i)).append("\n");
        }
        return stringBuilder;
    }

}
