package zos.shell.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class Environment {

    private static Environment INSTANCE;
    private Map<String, String> variables = new LinkedHashMap<>();

    private Environment() {
    }

    public static Environment getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Environment();
        }
        return INSTANCE;
    }

    public String getValueByKeyName(String key) {
        return variables.get(key.toUpperCase());
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariable(String key, String value) {
        variables.put(key.toUpperCase(), value.toUpperCase());
    }

}
