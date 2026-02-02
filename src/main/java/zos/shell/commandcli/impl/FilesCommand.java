package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class FilesCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "files";
    }

    @Override
    protected String description() {
        return "List local files for the current dataset";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        var result = ControllerFactoryContainerHolder.container()
                .getLocalFilesController()
                .files(ctx.currDataset);

        ctx.terminal.println(result.toString());
        ctx.searchCache = new SearchCache("files", result);
    }
}
