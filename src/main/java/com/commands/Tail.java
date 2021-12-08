package com.commands;

import com.Constants;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zosjobs.GetJobs;

import java.util.List;

public class Tail extends JobLog {

    public Tail(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll) {
        super(terminal, getJobs, isAll);
    }

    public void tail(String[] params) {
        List<String> output;
        try {
            output = getJobLog(params[1]);
        } catch (Exception e) {
            if (e.getMessage().contains("timeout")) {
                terminal.println(Constants.TAIL_TIMEOUT_MSG);
                return;
            }
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
            return;
        }
        var size = output.size();
        var lines = 0;
        if (params.length == 3) {
            try {
                lines = Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return;
            }
        }

        if (lines > 0) {
            if (lines < size) {
                for (int i = size - lines; i < size; i++)
                    terminal.println(output.get(i));
            } else output.forEach(terminal::println);
        } else {
            int LINES_LIMIT = 25;
            if (size > LINES_LIMIT) {
                for (int i = size - LINES_LIMIT; i < size; i++)
                    terminal.println(output.get(i));
            } else output.forEach(terminal::println);
        }
    }

}
