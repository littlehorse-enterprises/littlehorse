using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// The <c>ExternalEventNodeOutput</c> class represents a specialized NodeOutput.
/// It allows for setting timeouts and other configurations specific to external events outputs.
/// </summary>
public class ExternalEventNodeOutput : NodeOutput
{
    private Type? _payloadType;
    private CorrelatedEventConfig? _correlatedEventConfig;
    private string ExternalEventDefName { get; }

    /// <summary>
    /// Initializes a new instance of the <see cref="ExternalEventNodeOutput"/> class.
    /// </summary>
    /// <param name="nodeName">The specified node name.</param>
    /// <param name="externalEventDefName">The external event definition name.</param>
    /// <param name="parent">The workflow thread where the ExternalEventNodeOutput belongs to.</param>
    public ExternalEventNodeOutput(string nodeName, string externalEventDefName, WorkflowThread parent)
        : base(nodeName, parent)

    {
        ExternalEventDefName = externalEventDefName;
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
        if (_correlatedEventConfig == null) _correlatedEventConfig = new CorrelatedEventConfig();

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
        if (_correlatedEventConfig == null) _correlatedEventConfig = new CorrelatedEventConfig();
        return this;
    }

    /// <summary>
    /// Adds CorrelatedEventConfig to an ExternalEventNode.
    /// </summary>
    /// <returns>The ExternalEventNodeOutput.</returns>    
    public ExternalEventNodeOutput WithCorrelatedEventConfig(CorrelatedEventConfig config)
    {
        _correlatedEventConfig = config;
        return this;
    }


    /// <summary>
    /// Returns a PutExternalEventDefRequest for registering this external event definition.
    /// </summary>
    public PutExternalEventDefRequest ToPutExternalEventDefRequest()
    {
        var req = new PutExternalEventDefRequest
        {
            Name = ExternalEventDefName,
            ContentType = LHMappingHelper.DotNetTypeToReturnType(_payloadType)
        };
        if (_correlatedEventConfig != null)
            req.CorrelatedEventConfig = _correlatedEventConfig;
        return req;
    }
    /// <summary>
    /// Get the CorrelatedEventConfig
    /// </summary>
    /// <returns>The ExternalEventNodeOutput.</returns>
    public CorrelatedEventConfig GetCorrelatedEventConfig()
    {
        return _correlatedEventConfig ?? new CorrelatedEventConfig();
    }
    
    /// <summary>
    /// Registers the event definition with the specified payload type.
    /// </summary>
    /// <param name="payloadType">The .NET type of the event payload.</param>
    public ExternalEventNodeOutput RegisteredAs(Type payloadType)
    {
        _payloadType = payloadType;
        Parent.RegisterExternalEventDef(this);
        return this;
    }

}