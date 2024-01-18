package zos.shell.service.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(SearchCacheService.class);

    public SearchCacheService() {
        LOG.debug("*** SearchCacheService ***");
    }

    public List<String> search(final SearchCache output, final String text) {
        LOG.debug("*** search ***");
        List<String> results = new ArrayList<>();
        if (output == null) {
            results.add("nothing to search for...");
            return results;
        }
        results = Arrays.stream(output.getOutput().toString().split("\n"))
                .filter(line -> line.toUpperCase().contains(text.toUpperCase()))
                .collect(Collectors.toList());
        if (results.isEmpty()) {
            results.add("no results found...");
            return results;
        }
        return results;
    }

}
