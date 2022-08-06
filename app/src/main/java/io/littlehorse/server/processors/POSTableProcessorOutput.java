package io.littlehorse.server.processors;

import io.littlehorse.common.model.POSTable;
import io.littlehorse.server.model.internal.IndexEntryAction;

public class POSTableProcessorOutput {
    public POSTable<?> t;
    public IndexEntryAction action;

    public POSTableProcessorOutput(POSTable<?> theT) {
        t = theT;
    }

    public POSTableProcessorOutput(IndexEntryAction action) {
        this.action = action;
    }
}
