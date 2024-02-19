package zos.shell.singleton;

import java.util.HashMap;
import java.util.Map;

public class CheckSumSingleton {

    public static Map<String, String> checksums = new HashMap<>();

    private static class Holder {
        private static final CheckSumSingleton instance = new CheckSumSingleton();
    }

    private CheckSumSingleton() {
    }

    public static CheckSumSingleton getInstance() {
        return CheckSumSingleton.Holder.instance;
    }

    public String get(final String target) {
        return checksums.get(target);
    }

    public void put(final String target, final String value) {
        checksums.put(target, value);
    }

}
