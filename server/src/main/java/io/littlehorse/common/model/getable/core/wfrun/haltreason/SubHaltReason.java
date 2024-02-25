package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;

public interface SubHaltReason {
    public boolean isResolved(ThreadRunModel haltedThreadRun);
}
