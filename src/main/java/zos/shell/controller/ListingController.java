package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.list.ListingService;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;

public class ListingController {

    private static final Logger LOG = LoggerFactory.getLogger(ListingController.class);

    private final ListingService listingService;

    public ListingController(final ListingService listingService) {
        LOG.debug("*** ListingController ***");
        this.listingService = listingService;
    }

    public ResponseStatus ls(final String member, final String dataset) {
        LOG.debug("*** ls 1 ***");
        try {
            listingService.ls(member, dataset, true, false);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus(errMsg != null ? errMsg : e.getMessage(), false);
        }
        return new ResponseStatus("success", true);
    }

    public ResponseStatus ls(final String dataset) {
        LOG.debug("*** ls 2 ***");
        try {
            listingService.ls(null, dataset, true, false);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus(errMsg != null ? errMsg : e.getMessage(), false);
        }
        return new ResponseStatus("success", true);
    }

    public ResponseStatus lsl(final String dataset, final boolean isAttributes) {
        LOG.debug("*** lsl 1 ***");
        return this.lsl(null, dataset, isAttributes);
    }

    public ResponseStatus lsl(final String member, final String dataset, final boolean isAttributes) {
        LOG.debug("*** lsl 2 ***");
        try {
            listingService.ls(member, dataset, false, isAttributes);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus(errMsg != null ? errMsg : e.getMessage(), false);
        }
        return new ResponseStatus("success", true);
    }

}
