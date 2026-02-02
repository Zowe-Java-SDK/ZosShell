package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class DownloadJobCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "downloadjob";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"dj"};
    }

    @Override
    protected String description() {
        return "Download a job output";
    }

    @Override
    protected String usage() {
        return "downloadjob [OPTION] <JOBNAME>";
    }

    @Override
    protected Options options() {
        Options opts = new Options();
        opts.addOption(Option.builder("a")
                .longOpt("all")
                .desc("Download all job steps")
                .build());
        return opts;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            ctx.terminal.println("Usage: downloadjob [OPTION] <JOBNAME>");
            return;
        }

        boolean all = cmd.hasOption("a");
        var controller = ControllerFactoryContainerHolder.container()
                .getDownloadJobController(ctx.zosConnection, all, ctx.timeout);
        String result = controller.downloadJob(args.get(0));
        ctx.terminal.println(result);
    }
}
