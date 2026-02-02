package zos.shell.state;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.commandcli.CommandRouter;
import zos.shell.resolver.HistoryCommandResolver;
import zos.shell.singleton.HistorySingleton;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.utility.PromptUtil;
import zos.shell.utility.StrUtil;

import java.util.Arrays;

public class ShellStateMachine {

    private static final Logger LOG = LoggerFactory.getLogger(ShellStateMachine.class);

    private final TextIO textIO;
    private final TextTerminal<?> terminal;
    private final CommandRouter commandRouter;
    private final HistoryCommandResolver historyResolver;

    private ShellState state = ShellState.READ_INPUT;
    private String input;
    private String[] tokens;

    public ShellStateMachine(TextIO textIO) {
        this.textIO = textIO;
        this.terminal = TerminalSingleton.getInstance().getTerminal();
        this.commandRouter = new CommandRouter(terminal);
        this.historyResolver = new HistoryCommandResolver(terminal,
                HistorySingleton.getInstance().getHistory());
    }

    public void run() {
        LOG.debug("*** ShellStateMachine.run ***");

        while (state != ShellState.EXIT) {
            switch (state) {
                case READ_INPUT:
                    readInput();
                    break;
                case PROCESS_INPUT:
                    processInput();
                    break;
                case EXIT:
                    // no-op
                    break;
                default:
                    throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    private void readInput() {
        input = textIO.newStringInputReader()
                .withMaxLength(80)
                .read(PromptUtil.getPrompt());

        if (isExitCommand(input)) {
            state = ShellState.EXIT;
        } else {
            state = ShellState.PROCESS_INPUT;
        }
    }

    private void processInput() {
        // font size change short-circuit
        if (isFontSizeChanged()) {
            terminal.println("Font size updated.");
            state = ShellState.READ_INPUT;
            return;
        }

        tokens = StrUtil.stripEmptyStrings(input.trim().split("\\s+"));
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
        return "end".equalsIgnoreCase(input)
                || "exit".equalsIgnoreCase(input)
                || "quit".equalsIgnoreCase(input);
    }

    private boolean isFontSizeChanged() {
        if (TerminalSingleton.getInstance().isFontSizeChanged()) {
            TerminalSingleton.getInstance().setFontSizeChanged(false);
            return true;
        }
        return false;
    }
}

