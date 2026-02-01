package zos.shell.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.singleton.TerminalSingleton;

public final class PromptUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PromptUtil.class);

    private PromptUtil() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressWarnings("SameReturnValue")
    public static String getPrompt() {
        LOG.debug("*** getPrompt ***");
        var controllerFactoryContainer = ControllerFactoryContainerHolder.container();
        var envVariableController = controllerFactoryContainer.getEnvVariableController();
        var promptStr = envVariableController.getValueByEnv("PROMPT");
        if (promptStr != null && !promptStr.isBlank()) {
            int startIndex = promptStr.indexOf("$(");
            if (startIndex != -1) {
                int endIndex = promptStr.indexOf(")");
                if (endIndex != 1) {
                    var valueStr = promptStr.substring(startIndex + 2, endIndex);
                    var replacePromptStr = envVariableController.getValueByEnv(valueStr);
                    if (replacePromptStr != null && !replacePromptStr.isBlank()) {
                        promptStr = promptStr.replace("$(" + valueStr + ")", replacePromptStr);
                    }
                }
            }
            return promptStr + Constants.DEFAULT_PROMPT;
        }
        return Constants.DEFAULT_PROMPT;
    }

    public static String getPromptInfo(final String promptMsg, final boolean isMask) {
        LOG.debug("*** getPromptInfo ***");
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
