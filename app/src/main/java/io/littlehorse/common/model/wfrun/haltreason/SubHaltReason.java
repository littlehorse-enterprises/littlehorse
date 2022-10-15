package io.littlehorse.common.model.wfrun.haltreason;

import io.littlehorse.common.model.wfrun.WfRun;

public interface SubHaltReason {
    public boolean isResolved(WfRun wfRun);
}
