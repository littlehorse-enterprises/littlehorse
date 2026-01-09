package io.littlehorse.common.model.getable.core.wfrun;

import io.littlehorse.common.model.getable.objectId.ArchivedThreadRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import java.util.Iterator;
import java.util.List;

public class ThreadRunIterator implements Iterator<ThreadRunModel> {

    private WfRunIdModel wfRunId;
    private List<ThreadRunModel> threadRuns;
    private int maxThreadRunNumber;
    private GetableManager getableManager;
    int currentIndex = 0;

    public ThreadRunIterator(
            WfRunIdModel wfRunId,
            List<ThreadRunModel> threadRuns,
            int maxThreadRunNumber,
            GetableManager getableManager) {
        this.wfRunId = wfRunId;
        this.threadRuns = threadRuns;
        this.maxThreadRunNumber = maxThreadRunNumber;
        this.getableManager = getableManager;
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < maxThreadRunNumber;
    }

    @Override
    public ThreadRunModel next() {
        // Check local storage
        for (ThreadRunModel threadRun : threadRuns) {
            if (threadRun.getNumber() == this.currentIndex) {
                currentIndex++;
                return threadRun;
            }
        }

        // Obtain from archived storage
        ArchivedThreadRunModel potentialThreadRun =
                getableManager.get(new ArchivedThreadRunIdModel(this.wfRunId, currentIndex));

        if (potentialThreadRun != null) {
            currentIndex++;
            return potentialThreadRun.getThreadRun();
        }

        throw new IndexOutOfBoundsException("No more items to iterate.");
    }
}
