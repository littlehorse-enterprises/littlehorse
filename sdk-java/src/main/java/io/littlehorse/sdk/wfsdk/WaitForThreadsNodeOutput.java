package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.LHErrorType;

/**
 * The `WaitForThreadsNodeOutput` interface represents a specialized NodeOutput
 * used to manage parallel thread executions and control their behavior during
 * workflow execution.
 *
 * <p>When using this interface, you can set a policy that determines how the
 * node should handle waiting for threads' parallel executions:
 * {@link WaitForThreadsPolicy#STOP_ON_FAILURE}: Stops the entire execution for parent and children threads
 * when a single node fails
 *
 * @see WaitForThreadsPolicy
 * @author Eduwer Camacaro
 */
public interface WaitForThreadsNodeOutput extends NodeOutput {

    /**
     * Specifies a Failure Handler to run in case any of the ThreadRun's that we are waiting for in this
     * WaitForThreadsNode fails with a specific EXCEPTION.
     * @param exceptionName the exception name
     * @param handler the WorkflowThread defining the failure handler
     * @return this WaitForThreadsNodeOutput
     */
    WaitForThreadsNodeOutput handleExceptionOnChild(String exceptionName, ThreadFunc handler);

    /**
     * Specifies a Failure Handler to run in case any of the ThreadRun's that we are waiting for in this
     * WaitForThreadsNode fails with a specific ERROR.
     * @param exceptionName the ERROR type
     * @param handler the WorkflowThread defining the failure handler
     * @return this WaitForThreadsNodeOutput
     */
    WaitForThreadsNodeOutput handleErrorOnChild(LHErrorType error, ThreadFunc handler);

    /**
     * Specifies a Failure Handler to run in case any of the ThreadRun's that we are waiting for in this
     * WaitForThreadsNode fails with any Failure.
     * @param handler the WorkflowThread defining the failure handler
     * @return this WaitForThreadsNodeOutput
     */
    WaitForThreadsNodeOutput handleAnyFailureOnChild(ThreadFunc handler);
}
