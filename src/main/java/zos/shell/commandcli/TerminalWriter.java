package zos.shell.commandcli;

import org.beryx.textio.TextTerminal;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Writer;

public class TerminalWriter extends Writer {

    private final TextTerminal<?> terminal;

    public TerminalWriter(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    @Override
    public void write(char @NonNull [] buff, int off, int len) {
        terminal.print(new String(buff, off, len));
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

}
