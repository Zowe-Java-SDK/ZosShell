package zos.shell.state;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandRouter;
import zos.shell.resolver.HistoryCommandResolver;
import zos.shell.service.terminal.TerminalOutputService;
import zos.shell.singleton.HistorySingleton;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.utility.PromptUtil;
import zos.shell.utility.StrUtil;

import java.util.Arrays;

public class ShellStateMachine {

    private static final Logger LOG = LoggerFactory.getLogger(ShellStateMachine.class);

    private final TextIO textIO;
    private final CommandRouter commandRouter;
    private final HistoryCommandResolver historyResolver;
    private final TerminalOutputService outputService;

    private ShellState state = ShellState.READ_INPUT;
    private String input;

    public ShellStateMachine(TextIO textIO) {
        LOG.debug("*** ShellStateMachine ***");
        this.textIO = textIO;
        TextTerminal<?> terminal = TerminalSingleton.getInstance().getTerminal();
        this.outputService = new TerminalOutputService(terminal);
        this.commandRouter = new CommandRouter(terminal);
        this.historyResolver = new HistoryCommandResolver(terminal,
                HistorySingleton.getInstance().getHistory());
    }

    public void run() {
        LOG.debug("*** run ***");
        while (state != ShellState.EXIT) {
            switch (state) {
                case READ_INPUT:
                    readInput();
                    break;
                case PROCESS_INPUT:
                    processInput();
                    break;
                default:
                    throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    private void readInput() {
        LOG.debug("*** readInput ***");
        input = textIO
                .newStringInputReader()
                .withMaxLength(80)
                .read(PromptUtil.getPrompt());

        if (isExitCommand(input)) {
            state = ShellState.EXIT;
        } else {
            state = ShellState.PROCESS_INPUT;
        }
    }

    private void processInput() {
        LOG.debug("*** processInput ***");
        // font size change short-circuit
        if (isFontSizeChanged()) {
            outputService.println("Font size updated.");
            state = ShellState.READ_INPUT;
            return;
        }

        var tokens = StrUtil.stripEmptyStrings(input.trim().split("\\s+"));
        if (tokens.length == 0) {
            state = ShellState.READ_INPUT;
            return;
        }

        // history commands
        if (tokens[0].startsWith("!")) {
            tokens = historyResolver.resolve(tokens);
            if (tokens == null) {
                state = ShellState.READ_INPUT;
                return;
            }
        }

        // optional prompt prefix
        if (tokens[0].equalsIgnoreCase(PromptUtil.getPrompt()) && tokens.length > 1) {
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
        }

        commandRouter.routeCommand(String.join(" ", tokens));
        state = ShellState.READ_INPUT;
    }

    private boolean isExitCommand(String input) {
        LOG.debug("*** isExitCommand ***");
        return "end".equalsIgnoreCase(input)
                || "exit".equalsIgnoreCase(input)
                || "quit".equalsIgnoreCase(input);
    }

    private boolean isFontSizeChanged() {
        LOG.debug("*** isFontSizeChanged ***");
        if (TerminalSingleton.getInstance().isFontSizeChanged()) {
            TerminalSingleton.getInstance().setFontSizeChanged(false);
            return true;
        }
        return false;
    }

}
