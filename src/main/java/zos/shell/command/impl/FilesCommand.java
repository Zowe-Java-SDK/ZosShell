package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class FilesCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(FilesCommand.class);

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
        LOG.debug("*** FilesCommand.run ***");
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
