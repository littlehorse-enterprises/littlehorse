package io.littlehorse.common.model.getable.core.noderun;

import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This class is thrown by the NodeRunModel class when trying to advance. It wraps the
 * FailureModel class, and is handled by the ThreadRunModel. We are doing this to remove
 * the ThreadRunModel#fail() method, since the NodeRunModel should not 
 */
@Getter
@AllArgsConstructor
public class NodeFailureException extends Exception {

    /**
     * The LittleHorse Workflow Failure that was thrown.
     */
    private FailureModel failure;

    @Override
    public String getMessage() {
        return failure.getMessage();
    }
}
