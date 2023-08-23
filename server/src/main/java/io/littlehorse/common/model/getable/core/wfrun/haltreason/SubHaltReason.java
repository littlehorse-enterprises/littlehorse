package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;

public interface SubHaltReason {
    public boolean isResolved(WfRunModel wfRunModel);
}
