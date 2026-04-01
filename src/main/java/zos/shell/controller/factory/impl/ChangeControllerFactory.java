package zos.shell.controller.container.impl;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.ChangeConnController;
import zos.shell.controller.ChangeDirController;
import zos.shell.controller.ChangeWinController;
import zos.shell.controller.container.AbstractController;
import zos.shell.controller.container.type.ChangeControllerType;
import zos.shell.service.change.ChangeConnService;
import zos.shell.service.change.ChangeDirService;
import zos.shell.service.change.ChangeWinService;
import zos.shell.service.terminal.TerminalOutputService;

public class ChangeControllerFactory extends AbstractController<ChangeControllerType.Name> {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeControllerFactory.class);

    public ChangeConnController getChangeConnectionController(final TextTerminal<?> terminal) {
        LOG.debug("*** getChangeConnectionController ***");
        return this.getOrCreateController(
                ChangeControllerType.Name.CHANGE_CONNECTION,
                ChangeConnController.class,
                () -> new ChangeConnController(new ChangeConnService(terminal))
        );
    }

    public ChangeDirController getChangeDirectoryController() {
        LOG.debug("*** getChangeDirectoryController ***");
        return this.getOrCreateController(
                ChangeControllerType.Name.CHANGE_DIRECTORY,
                ChangeDirController.class,
                () -> new ChangeDirController(new ChangeDirService())
        );
    }

    public ChangeWinController getChangeWindowController(final TextTerminal<?> terminal) {
        LOG.debug("*** getChangeWindowController ***");
        return this.getOrCreateController(
                ChangeControllerType.Name.CHANGE_WINDOW,
                ChangeWinController.class,
                () -> {
                    var outputService = new TerminalOutputService(terminal);
                    return new ChangeWinController(
                            new ChangeWinService(terminal, outputService::redrawBufferedOutput)
                    );
                }
        );
    }

}
