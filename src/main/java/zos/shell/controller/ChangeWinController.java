package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.change.ChangeWinService;

public class ChangeWinController {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeWinController.class);

    private final ChangeWinService changeWinService;

    public ChangeWinController(final ChangeWinService changeWinService) {
        LOG.debug("*** ChangeWinController ***");
        this.changeWinService = changeWinService;
    }

    public String changeColorSettings(final String textColor, final String backGroundColor) {
        LOG.debug("*** changeColorSettings ***");
        var str = new StringBuilder();
        String result;
        result = changeWinService.setTextColor(textColor);
        str.append(result != null ? result + "\n" : "");
        result = changeWinService.setBackGroundColor(backGroundColor);
        str.append(result != null ? result : "");
        return str.toString();
    }

}
