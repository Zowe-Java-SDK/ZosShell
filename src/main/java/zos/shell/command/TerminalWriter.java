package zos.shell.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import zos.shell.service.terminal.TerminalOutputService;

import java.io.Writer;

public class TerminalWriter extends Writer {

    private final TerminalOutputService out;
    private final StringBuilder currentLine = new StringBuilder();

    public TerminalWriter(final TerminalOutputService out) {
        this.out = out;
    }

    @Override
    public void write(char @NonNull [] buff, int off, int len) {
        String text = new String(buff, off, len);
        this.currentLine.append(text);

        String all = this.currentLine
                .toString()
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        String[] parts = all.split("\n", -1);

        for (int i = 0; i < parts.length - 1; i++) {
            this.out.println(parts[i]);
        }

        this.currentLine.setLength(0);
        this.currentLine.append(parts[parts.length - 1]);
    }

    @Override
    public void flush() {
        if (this.currentLine.length() > 0) {
            this.out.print(this.currentLine.toString());
            this.currentLine.setLength(0);
        }
    }

    @Override
    public void close() {
        flush();
    }

}
