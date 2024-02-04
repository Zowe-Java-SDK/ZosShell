package zos.shell.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;

public final class PromptUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PromptUtil.class);

    private PromptUtil() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressWarnings("SameReturnValue")
    public static String getPrompt() {
        LOG.debug("*** getPrompt ***");
        return Constants.DEFAULT_PROMPT;
    }

}
