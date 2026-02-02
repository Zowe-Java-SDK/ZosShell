package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

import java.util.List;

public class DownloadCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "download";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"d"};
    }

    @Override
    protected String description() {
        return "Download a sequential dataset or member";
    }

    @Override
    protected String usage() {
        return "download [OPTION] <SOURCE>";
    }

    @Override
    protected Options options() {
        Options opts = new Options();
        opts.addOption(Option.builder("b")
                .longOpt("binary")
                .desc("Download in binary mode")
                .build());
        return opts;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        boolean isBinary = cmd.hasOption("b");
        var controller = ControllerFactoryContainerHolder.container()
                .getDownloadDsnController(ctx.zosConnection, isBinary, ctx.timeout);
        List<String> result = controller.download(ctx.currDataset, args.get(0));
        result.forEach(ctx.terminal::println);
    }
}
