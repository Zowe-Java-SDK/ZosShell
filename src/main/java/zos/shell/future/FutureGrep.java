package zos.shell.future;

import zos.shell.commands.Grep;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureGrep implements Callable<List<String>> {

    private final Grep grep;
    private final String currDataSet;
    private final String target;

    public FutureGrep(Grep grep, String currDataSet, String target) {
        this.grep = grep;
        this.currDataSet = currDataSet;
        this.target = target;
    }

    @Override
    public List<String> call() {
        return grep.search(currDataSet, target);
    }

}
