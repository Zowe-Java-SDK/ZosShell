package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.search.SearchCache;
import zos.shell.service.search.SearchCacheService;

import java.util.List;

public class SearchCacheController {

    private static final Logger LOG = LoggerFactory.getLogger(SearchCacheController.class);

    private final SearchCacheService searchCacheService;

    public SearchCacheController(final SearchCacheService searchCacheService) {
        LOG.debug("*** SearchCacheController ***");
        this.searchCacheService = searchCacheService;
    }

    public List<String> search(final SearchCache output, final String text) {
        LOG.debug("*** search ***");
        return searchCacheService.search(output, text);
    }

}
