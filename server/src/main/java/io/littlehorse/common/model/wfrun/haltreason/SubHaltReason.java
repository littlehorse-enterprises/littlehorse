package io.littlehorse.common.model.wfrun.haltreason;

import io.littlehorse.common.model.wfrun.WfRunModel;

public interface SubHaltReason {
    public boolean isResolved(WfRunModel wfRunModel);
}
