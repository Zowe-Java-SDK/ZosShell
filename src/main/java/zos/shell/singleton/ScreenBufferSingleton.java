package zos.shell.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@SuppressWarnings("unused")
public final class ScreenBufferSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenBufferSingleton.class);

    private static final int DEFAULT_MAX_LINES = 100;
    private static final ScreenBufferSingleton INSTANCE = new ScreenBufferSingleton();
    private final Deque<String> lines = new ArrayDeque<>();

    private int maxLines = DEFAULT_MAX_LINES;

    private ScreenBufferSingleton() {
        LOG.debug("*** ScreenBufferSingleton ***");
    }

    public static ScreenBufferSingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return INSTANCE;
    }

    public synchronized void addLine(final String line) {
        LOG.debug("*** addLine ***");
        if (line == null) {
            return;
        }

        lines.addLast(line);
        trimToLimit();
    }

    public synchronized void addLines(final String text) {
        LOG.debug("*** addLines ***");

        if (text == null || text.isEmpty()) {
            return;
        }

        String normalized = text
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        String[] split = normalized.split("\n", -1);

        for (int i = 0; i < split.length; i++) {
            if (i == split.length - 1 && split[i].isEmpty()) {
                continue;
            }
            addLine(split[i]);
        }
    }

    public synchronized List<String> getLines() {
        LOG.debug("*** getLines ***");
        return new ArrayList<>(lines);
    }

    public synchronized void clear() {
        LOG.debug("*** clear ***");
        lines.clear();
    }

    public synchronized void setMaxLines(final int maxLines) {
        LOG.debug("*** setMaxLines ***");
        if (maxLines <= 0) {
            throw new IllegalArgumentException("maxLines must be > 0");
        }
        this.maxLines = maxLines;
        trimToLimit();
    }

    private void trimToLimit() {
        LOG.debug("*** trimToLimit ***");
        while (lines.size() > maxLines) {
            lines.removeFirst();
        }
    }

}
