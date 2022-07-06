package com.future;

import com.commands.Concatenate;
import com.commands.Download;
import com.dto.ResponseStatus;
import org.beryx.textio.TextTerminal;

import java.util.concurrent.Callable;

public class FutureConcatenate extends Concatenate implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String member;

    public FutureConcatenate(TextTerminal<?> terminal, Download download, String dataSet, String member) {
        super(terminal, download);
        this.dataSet = dataSet;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        return this.cat(dataSet, member);
    }

}
