package zos.shell.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandHandler;

import java.util.HashMap;
import java.util.Map;

public final class CommandRegistrySingleton {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRegistrySingleton.class);

    private static final CommandRegistrySingleton INSTANCE = new CommandRegistrySingleton();

    private final Map<String, CommandHandler> commands = new HashMap<>();

    private CommandRegistrySingleton() {
        LOG.debug("*** CommandRegistrySingleton ***");
    }

    public static CommandRegistrySingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return INSTANCE;
    }

    public synchronized Map<String, CommandHandler> getRegistry() {
        LOG.debug("*** getRegistry ***");
        return new HashMap<>(commands);
    }

    public synchronized void setRegistry(final Map<String, CommandHandler> newCommands) {
        LOG.debug("*** setRegistry ***");
        commands.clear();
        if (newCommands != null) {
            commands.putAll(newCommands);
        }
    }

}
