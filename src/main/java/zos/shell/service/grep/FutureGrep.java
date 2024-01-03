package zos.shell.service.grep;

import zos.shell.service.dsn.concatenate.ConcatCmd;
import zos.shell.service.grep.GrepCmd;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureGrep extends Grep implements Callable<List<String>> {

    private final String dataset;
    private final String target;

    public FutureGrep(final ConcatCmd concatenate, final String dataset, final String target,
                      final String pattern, boolean withMember) {
        super(concatenate,pattern,withMember);
        this.dataset = dataset;
        this.target = target;
    }

    @Override
    public List<String> call() {
        return this.search(dataset, target);
    }

}
