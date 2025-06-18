package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.usermod.UsermodService;

public class UsermodController {

    private static final Logger LOG = LoggerFactory.getLogger(UsermodController.class);

    private final UsermodService usermodService;

    public UsermodController(final UsermodService usermodService) {
        LOG.debug("*** UsermodController ***");
        this.usermodService = usermodService;
    }

    public String change(final String flag) {
        LOG.debug("*** change ***");
        if ("-p".equalsIgnoreCase(flag)) {
            usermodService.changePassword();
            return "password changed";
        } else if ("-u".equalsIgnoreCase(flag)) {
            usermodService.changeUsername();
            return "username changed";
        } else {
            return Constants.INVALID_COMMAND;
        }
    }

}
