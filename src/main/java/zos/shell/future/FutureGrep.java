package zos.shell.future;

import zos.shell.commands.Grep;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureGrep implements Callable<List<String>> {

    private final Grep grep;
    private final String dataSet;
    private final String member;

    public FutureGrep(Grep grep, String dataSet, String member) {
        this.grep = grep;
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public List<String> call() throws Exception {
        return grep.search(dataSet, member);
    }

}
