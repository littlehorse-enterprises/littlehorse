using LittleHorse.Sdk.Common.Proto;
using Microsoft.AspNetCore.Builder;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// The <c>ExternalEventNodeOutput</c> class represents a specialized NodeOutput.
/// It allows for setting timeouts and other configurations specific to external events outputs.
/// </summary>
public class ExternalEventNodeOutput : NodeOutput
{

    // internal CorrelatedEventConfig _correlatedEventConfig;
    

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
        //should mask automatically
        bool shouldMaskAutomatically = (correlationId is WfRunVariable) && ((WfRunVariable)correlationId).IsMasked;
        Parent.SetCorrelationIdOnExternalEventNode(this, correlationId, shouldMaskAutomatically);
        //        if (correlatedEventConfig == null) correlatedEventConfig = CorrelatedEventConfig.getDefaultInstance();

        return this;
    }

    /// <summary>
    /// Adds a correlation id to an ExternalEventNode.
    /// </summary>
    /// <param name="correlationId">
    /// An object (variable or string literal) that is used as the correlation ID.
    /// </param>
    /// <param name="masked">
    /// A boolean value that is used to determine whether to mask the correlation ID.
    /// </param>
    /// <returns>The ExternalEventNodeOutput.</returns>
    public ExternalEventNodeOutput WithCorrelationId(object correlationId, bool masked)
    {
        Parent.SetCorrelationIdOnExternalEventNode(this, correlationId, masked);
        // if (correlatedEventConfig == null) correlatedEventConfig = CorrelatedEventConfig.getDefaultInstance();
        // CorrelatedEventConfig.
        return this;
    }

    //allow setting a CorrelatedEventConfig at registration?
    // public CorrelatedEventConfig GetCorrelatedEventConfig()
    // {
    //     return _correlatedEventConfig;
    // }
}