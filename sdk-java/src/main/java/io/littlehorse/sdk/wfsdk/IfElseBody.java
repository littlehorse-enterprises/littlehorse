package io.littlehorse.sdk.wfsdk;

/**
 * Functional interface representing a body of Workflow code to be executed inside an if or else
 * condition.
 *
 * <p>This interface should be implemented by the user of LittleHorse.
 */
public interface IfElseBody {
    /**
     * This is the body of Workflow Code.
     *
     * @param thread is a handle to the ThreadBuilder for the specific if/else block.
     */
    public void body(WorkflowThread thread);
}
