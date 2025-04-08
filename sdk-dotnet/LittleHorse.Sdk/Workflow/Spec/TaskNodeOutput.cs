using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents the output of a TaskNode.
/// </summary>
public class TaskNodeOutput : NodeOutput
{
    /// <summary>
    /// Initializes a new instance of the <see cref="TaskNodeOutput"/> class.
    /// </summary>
    /// <param name="nodeName">The specified node name.</param>
    /// <param name="parent">The workflow thread where the task output belongs to.</param>
    public TaskNodeOutput(string nodeName, WorkflowThread parent) : base(nodeName, parent)
    {
    }
    
    /// <summary>
    /// Overrides defaults from the Workflow or WorkflowThread and configures an Exponential Backoff Retry
    /// Policy for this TaskNode.
    /// </summary>
    /// <param name="policy">
    /// It is the ExponentialBackoffRetryPolicy for this TaskNode.
    /// </param>
    /// <returns>This TaskNodeOutput.</returns>
    public TaskNodeOutput WithExponentialBackoff(ExponentialBackoffRetryPolicy policy)
    {
        Parent.OverrideTaskExponentialBackoffPolicy(this, policy);
        return this;
    }
    
    /// <summary>
    /// Overrides defaults from the Workflow or WorkflowThread and configures simple retries with no delay
    /// on this TaskNode.
    /// </summary>
    /// <param name="retries">
    /// It is the number of times to retry failed executions of TaskRuns on this Task Node.
    /// </param>
    /// <returns>This TaskNodeOutput.</returns>
    public TaskNodeOutput WithRetries(int retries) 
    {
        Parent.OverrideTaskRetries(this, retries);
        return this;
    }
}