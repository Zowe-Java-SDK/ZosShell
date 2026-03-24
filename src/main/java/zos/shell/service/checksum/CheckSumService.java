package zos.shell.service.checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class CheckSumService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckSumService.class);

    private final Map<String, String> checksums = new HashMap<>();

    public CheckSumService() {
        LOG.debug("*** CheckSumService ***");
    }

    public void addCheckSum(final String target) {
        LOG.debug("*** addCheckSum ***");
        checksums.put(target, calculateCheckSum(target));
    }

    public String getCheckSum(final String target) {
        LOG.debug("*** getCheckSum ***");
        return checksums.get(target);
    }

    public String calculateCheckSum(final String target) {
        LOG.debug("*** calculateCheckSum ***");
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(target));
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            LOG.error("Error calculating checksum for {}", target, e);
            throw new RuntimeException(e);
        }
    }

}
