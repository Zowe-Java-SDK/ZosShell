package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.AbstractCommand;
import zos.shell.command.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class DownloadJobCommand extends AbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadJobCommand.class);

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
        return "downloadjob [OPTIONS] <JOBNAME>";
    }

    @Override
    protected Options options() {
        Options opts = new Options();
        opts.addOption(Option.builder("a")
                .longOpt("all")
                .desc("Download all job steps")
                .build());
        opts.addOption(Option.builder("i")
                .longOpt("id")
                .hasArg()
                .argName("jobId")
                .desc("Job ID associated with the download")
                .build());
        return opts;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** DownloadJobCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        boolean all = cmd.hasOption("a");
        String jobId = cmd.getOptionValue("i");
        if (jobId != null) {
            jobId = jobId.trim().toUpperCase();
        }

        var controller = ControllerFactoryContainerHolder
                .container()
                .getDownloadJobController(ctx.zosConnection, all, jobId, ctx.timeout);

        String result = controller.downloadJob(args.get(0));
        ctx.terminal.println(result);
    }

}
