namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// The <c>ExternalEventNodeOutput</c> class represents a specialized NodeOutput.
/// It allows for setting timeouts and other configurations specific to external events outputs.
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
    /// Adds a timeout to a Node.
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

    /// <summary>
    /// Adds a correlation id to an ExternalEventNode.
    /// </summary>
    /// <param name="correlationId">
    /// An object (variable or string literal) that is used as the correlation ID.
    /// </param>
    /// <returns>The ExternalEventNodeOutput.</returns>
    public ExternalEventNodeOutput WithCorrelationId(object correlationId)
    {
        Parent.SetCorrelationIdOnExternalEventNode(this, correlationId);
        return this;
    }
}