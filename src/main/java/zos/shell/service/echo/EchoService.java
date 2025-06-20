package zos.shell.service.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.EnvVariableController;

import java.util.ArrayList;
import java.util.List;

public class EchoService {

    private static final Logger LOG = LoggerFactory.getLogger(EchoService.class);

    private final EnvVariableController envVariableController;

    public EchoService(final EnvVariableController envVariableController) {
        LOG.debug("*** EchoService ***");
        this.envVariableController = envVariableController;
    }

    public enum QuoteType {

        DoubleQuote("\""),
        SingleQuote("'");

        private final String value;

        QuoteType(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    public String getEcho(String arg) {
        LOG.debug("*** getEcho ***");
        if (arg.contains("\"") || arg.contains("'")) {
            boolean isValid = validateEvenQuotes(arg);
            if (!isValid) {
                return "invalid input missing quote pair, try again";
            }
            if (arg.contains("\"")) {
                arg = extractQuotedData(arg, QuoteType.DoubleQuote);
            }
            if (arg.contains("'")) {
                arg = extractQuotedData(arg, QuoteType.SingleQuote);
            }
        }
        var envVariables = getDollarSignVariable(arg);
        // replace dollar sign env variables with actual values if any
        for (final String envVariable : envVariables) {
            var value = envVariableController.getValueByEnv(envVariable);
            if (!value.isBlank()) {
                var original = "$" + envVariable;
                arg = arg.replace(original, value);
            }
        }
        return arg;
    }

    private static List<String> getDollarSignVariable(final String arg) {
        LOG.debug("*** getDollarSignVariable ***");
        var envVariables = new ArrayList<String>();
        if (arg.contains("$")) {
            for (int i = 0; i < arg.length(); i++) {
                boolean found = false;
                if (arg.charAt(i) == '$') {
                    found = true;
                    var str = new StringBuilder();
                    for (int j = i + 1; j < arg.length() && (arg.charAt(j) != ' ' || arg.charAt(j) != '$'); j++) {
                        str.append(arg.charAt(j));
                    }
                    envVariables.add(str.toString());
                }
                if (found) {
                    i++;
                }
            }
        }
        return envVariables;
    }

    private static String extractQuotedData(String str, QuoteType quoteType) {
        LOG.debug("*** extractQuotedData ***");
        char quoteChar;
        if ("\"".equals(quoteType.getValue())) {
            quoteChar = '"';
        } else {
            quoteChar = '\'';
        }
        long numberOfQuotes = str.chars().filter(ch -> ch == quoteChar).count();
        if (numberOfQuotes == 0) {
            return str;
        }

        int index = str.indexOf(quoteChar);
        String result = str.substring(0, index);
        result = result + str.substring(index + 1, str.length() - 1);

        return extractQuotedData(result, quoteType);
    }

    private static boolean validateEvenQuotes(final String str) {
        LOG.debug("*** validateEvenQuotes ***");
        long numberOfDoubleQuotes = str.chars().filter(ch -> ch == '\"').count();
        long numberOfSingleQuotes = str.chars().filter(ch -> ch == '\'').count();
        return (numberOfDoubleQuotes % 2 == 0) && (numberOfSingleQuotes % 2 == 0);
    }

}
