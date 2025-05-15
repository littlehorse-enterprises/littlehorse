namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// The <c>WaitForThreadsNodeOutput</c> class represents a specialized NodeOutput
/// used to manage parallel thread executions and control their behavior during
/// workflow execution.
///
/// When using this interface, you can set a policy that determines how the
/// node should handle waiting for threads' parallel executions:
/// 
/// </summary>
public class ExternalEventNodeOutput: NodeOutput
{
    /// <summary>
    /// Initializes a new instance of the <see cref="ExternalEventNodeOutput"/> class.
    /// </summary>
    /// <param name="nodeName">The specified node name.</param>
    /// <param name="parent">The workflow thread where the ExternalEventNodeOutput belongs to.</param>
    public ExternalEventNodeOutput(string nodeName, WorkflowThread parent)
        : base(nodeName, parent)
    {
    }
    
    /// <summary>
    /// Adds a timeout to a Node. Valid on TaskRuns and ExternalEvents.
    /// </summary>
    /// <param name="timeoutSeconds">
    /// The timeout length.
    /// </param>
    /// <returns>The ExternalEventNodeOutput.</returns>
    public ExternalEventNodeOutput WithTimeout(int timeoutSeconds) 
    {
        Parent.AddTimeoutToExtEvtNode(this, timeoutSeconds);
        
        return this;
    }
}