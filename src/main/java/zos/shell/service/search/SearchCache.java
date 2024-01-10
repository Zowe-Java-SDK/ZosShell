package zos.shell.service.search;

public class SearchCache {

    private final String name;
    private final StringBuilder output;

    public SearchCache(String name, StringBuilder output) {
        this.name = name;
        this.output = output;
    }

    public String getName() {
        return name;
    }

    public StringBuilder getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "Output{" +
                "name='" + name + '\'' + '}';
    }

}
