using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Workflow.Spec
{
    /// <summary>
    /// Represents the output of a ThrowEvent node in a workflow, allowing event definition registration.
    /// </summary>
    public class ThrowEventNodeOutput
    {
        private readonly WorkflowThread _parent;
        private readonly string _eventName;
        private Type? _payloadType;

        /// <summary>
        /// Initializes a new instance of the <see cref="ThrowEventNodeOutput"/> class.
        /// </summary>
        /// <param name="eventName">The name of the workflow event definition.</param>
        /// <param name="parent">The parent workflow thread.</param>
        public ThrowEventNodeOutput(string eventName, WorkflowThread parent)
        {
            _eventName = eventName;
            _parent = parent;
        }

        /// <summary>
        /// Registers the event definition with the specified payload type.
        /// </summary>
        /// <param name="payloadType">The .NET type of the event payload.</param>
        public void RegisteredAs(Type? payloadType)
        {
            _payloadType = payloadType;
            _parent.RegisterWorkflowEventDef(this);
        }

        /// <summary>
        /// Returns a <see cref="PutWorkflowEventDefRequest"/> for registering this workflow event definition.
        /// </summary>
        /// <returns>The request object for event definition registration.</returns>
        /// <exception cref="InvalidOperationException">Thrown if <see cref="RegisteredAs"/> was not called before use.</exception>
        public PutWorkflowEventDefRequest ToPutWorkflowEventDefRequest()
        {
            return new PutWorkflowEventDefRequest
            {
                Name = _eventName,
                ContentType = LHMappingHelper.DotNetTypeToReturnType(_payloadType!) 
            };
        }
    }
}
