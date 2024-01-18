package zos.shell.service.job.tail;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.browse.BrowseLog;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.Arrays;
import java.util.List;

public class Tail extends BrowseLog {

    private static final Logger LOG = LoggerFactory.getLogger(Tail.class);

    private final TextTerminal<?> terminal;

    public Tail(final TextTerminal<?> terminal, final JobGet retrieve, boolean isAll, final long timeout) {
        super(retrieve, isAll, timeout);
        LOG.debug("*** Tail ***");
        this.terminal = terminal;
    }

    public ResponseStatus tail(final String[] params) {
        LOG.debug("*** tail ***");
        final ResponseStatus result = browseLog(params[1]);
        if (!result.isStatus()) {
            return result;
        }
        var output = Arrays.asList(result.getMessage().split("\n"));

        int size = output.size();
        int lines = 0;
        if (params.length == 3) {
            if (!"all".equalsIgnoreCase(params[2])) {
                lines = Integer.parseInt(params[2]);
            }
        }
        if (params.length == 4) {
            lines = Integer.parseInt(params[2]);
        }

        if (lines > 0) {
            if (lines < size) {
                return display(lines, size, output);
            } else {
                displayAll(output);
                return result;
            }
        } else {
            int LINE_LIMIT = 25;
            if (size > LINE_LIMIT) {
                return display(LINE_LIMIT, size, output);
            } else {
                displayAll(output);
                return result;
            }
        }
    }

    private void displayAll(final List<String> output) {
        LOG.debug("*** displayAll ***");
        output.forEach(terminal::println);
    }

    private ResponseStatus display(final int lines, final int size, final List<String> output) {
        LOG.debug("*** display ***");
        var str = new StringBuilder();
        for (var i = size - lines; i < size; i++) {
            terminal.println(output.get(i));
            str.append(output.get(i)).append("\n");
        }
        return new ResponseStatus(str.toString(), true);
    }

}