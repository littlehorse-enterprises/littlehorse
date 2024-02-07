package io.littlehorse.sdk.wfsdk;

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

    // WaitForThreadsNodeOutput handleExceptionOnChild(String exceptionName, WorkflowThread handler);

    // WaitForThreadsNodeOutput handleErrorOnChild(LHErrorType error, WorkflowThread handler);

    // WaitForThreadsNodeOutput handleAnyFailureOnChild(WorkflowThread handler);
}
