package io.littlehorse.common.model;

import io.littlehorse.common.model.wfrun.WfRun;

/**
 * Allows to apply variable modifications within the context of the ThreadRun that owns it
 */
@FunctionalInterface
public interface VariableModification {
    /**
     * Apply a variable modification within the context of the ThreadRun that owns the variable
     * @param wfRunId the wfRunId of the ThreadRun that owns the variable
     * @param threadRunNumber the threadRunNumber of the ThreadRun that owns the variable
     * @param wfRun the wfRun of the ThreadRun that owns the variable
     */
    void apply(String wfRunId, int threadRunNumber, WfRun wfRun);
}
