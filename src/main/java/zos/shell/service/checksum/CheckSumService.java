package zos.shell.service.checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.singleton.CheckSumSingleton;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSumService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckSumService.class);

    private static final CheckSumSingleton INSTANCE = CheckSumSingleton.getInstance();

    public CheckSumService() {
        LOG.debug("*** CheckSumService ***");
    }

    public void addCheckSum(final String target) {
        INSTANCE.put(target, calculateCheckSum(target));
    }

    public String getCheckSum(final String target) {
        return INSTANCE.get(target);
    }

    public String calculateCheckSum(final String target) {
        byte[] hash = new byte[0];
        try {
            byte[] data = Files.readAllBytes(Paths.get(target));
            hash = MessageDigest.getInstance("MD5").digest(data);
        } catch (IOException | NoSuchAlgorithmException ignored) {
        }
        return new BigInteger(1, hash).toString(16);
    }

}
