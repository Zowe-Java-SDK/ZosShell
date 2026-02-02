package zos.shell.commandcli;

import org.apache.commons.cli.Options;

public abstract class NoOptionCommand extends AbstractCommand {

    @Override
    protected final Options options() {
        return new Options();
    }

}
