package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.WaitForThreadsPolicy;

/**
 * The `WaitForThreadsNodeOutput` interface represents a specialized NodeOutput
 * used to manage parallel thread executions and control their behavior during
 * workflow execution.
 *
 * <p>When using this interface, you can set a policy that determines how the
 * node should handle waiting for threads' parallel executions:
 * {@link WaitForThreadsPolicy#WAIT_FOR_COMPLETION}: Waits until every thread completes
 *  before continuing with the execution.
 * {@link WaitForThreadsPolicy#STOP_ON_FAILURE}: Stops the entire execution for parent and children threads
 * when a single node fails
 *
 * @see WaitForThreadsPolicy
 * @author Eduwer Camacaro
 */
public interface WaitForThreadsNodeOutput extends NodeOutput {

    /**
     * Sets the policy that determines how the node should handle waiting for
     * threads' parallel executions.
     * Usage example:
     * WaitForThreadsNodeOutput output = thread.waitForThreads(...)
     * output.withPolicy(WaitForThreadsPolicy.WAIT_FOR_COMPLETION);
     *
     * @param policy The policy to be used by the node to determine whether to
     *               continue with the execution or not.
     * @return A reference to the updated WaitForThreadsNodeOutput.
     * @see WaitForThreadsPolicy
     */
    WaitForThreadsNodeOutput withPolicy(WaitForThreadsPolicy policy);
}
