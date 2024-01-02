package zos.shell.service.env;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnvVarCmd {

    private static EnvVarCmd INSTANCE;
    private final Map<String, String> variables = new LinkedHashMap<>();

    private EnvVarCmd() {
    }

    public static EnvVarCmd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EnvVarCmd();
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
