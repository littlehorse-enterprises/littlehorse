package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import java.io.Serializable;

public interface TaskNodeOutput extends NodeOutput, Serializable {

    /**
     * Overrides defaults from the Workflow or WorkflowThread and configures an Exponential Backoff Retry
     * Policy for this TaskNode.
     * @param policy is the ExponentialBackoffRetryPolicy for this TaskNode.
     * @return this TaskNodeOutput.
     */
    TaskNodeOutput withExponentialBackoff(ExponentialBackoffRetryPolicy policy);

    /**
     * Overrides defaults from the Workflow or WorkflowThread and configures simple retries with no delay
     * on this TaskNode.
     * @param retries is the number of times to retry failed executions of TaskRuns on this Task Node.
     */
    TaskNodeOutput withRetries(int retries);
}
