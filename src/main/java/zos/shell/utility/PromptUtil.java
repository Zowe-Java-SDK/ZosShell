package zos.shell.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.singleton.TerminalSingleton;

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

    public static String getPromptInfo(final String promptMsg, final boolean isMask) {
        LOG.debug("*** getPrompt ***");
        TerminalSingleton.getInstance().setDisableKeys(true);
        var result = TerminalSingleton.getInstance()
                .getMainTextIO()
                .newStringInputReader()
                .withMaxLength(80)
                .withInputMasking(isMask)
                .read(promptMsg);
        TerminalSingleton.getInstance().setDisableKeys(false);
        return result;
    }

}
