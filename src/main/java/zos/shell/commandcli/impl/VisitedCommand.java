package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;

import java.util.List;

public class VisitedCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "visited";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"v"};
    }

    @Override
    protected String description() {
        return "List visited datasets";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (CommandContext.dataSets.isEmpty()) {
            ctx.terminal.println(Constants.NO_VISITED_DATASETS);
            return;
        }
        CommandContext.dataSets.keySet().forEach(host -> {
            List<String> lst = CommandContext.dataSets.get(host);
            lst.forEach(d -> ctx.terminal.println(
                    String.format("%" + ctx.currDatasetMax + "s -> %s", d.toUpperCase(), host)));
        });
    }
}
