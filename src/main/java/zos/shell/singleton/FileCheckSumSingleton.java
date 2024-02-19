package zos.shell.singleton;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class FileCheckSumSingleton {

    public static Map<String, String> checksums = new HashMap<>();

    private static class Holder {
        private static final FileCheckSumSingleton instance = new FileCheckSumSingleton();
    }

    private FileCheckSumSingleton() {
    }

    public static FileCheckSumSingleton getInstance() {
        return FileCheckSumSingleton.Holder.instance;
    }

    public void addCheckSum(final String target) {
        checksums.put(target, calculateCheckSum(target));
    }

    public String getCacheCheckSum(final String target) {
        return checksums.get(target);
    }

    public String getCheckSum(final String target) {
        return calculateCheckSum(target);
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
