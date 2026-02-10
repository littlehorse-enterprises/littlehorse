namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Fluent helper for registering interrupt ExternalEventDef metadata.
/// </summary>
public sealed class InterruptHandler
{
    private readonly Workflow _workflow;
    private readonly string _interruptName;
    private bool _eventTypeRegistered;

    internal InterruptHandler(Workflow workflow, string interruptName)
    {
        _workflow = workflow;
        _interruptName = interruptName;
    }

    /// <summary>
    /// Registers an ExternalEventDef for this interrupt handler with the given payload type.
    /// </summary>
    /// <param name="payloadType">
    /// The .NET type of the interrupt event payload. If null, the ExternalEventDef will have a null payload.
    /// </param>
    public void WithEventType(Type? payloadType)
    {
        if (_eventTypeRegistered)
        {
            throw new InvalidOperationException($"Interrupt event type already registered: {_interruptName}");
        }

        _eventTypeRegistered = true;
        _workflow.AddExternalEventDefToRegister(
            new InterruptExternalEventDefRegistration(_interruptName, payloadType));
    }
}
