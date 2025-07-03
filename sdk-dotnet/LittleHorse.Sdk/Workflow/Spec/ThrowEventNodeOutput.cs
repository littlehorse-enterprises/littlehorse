using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;

namespace LittleHorse.Sdk.Workflow.Spec
{
    public class ThrowEventNodeOutput
    {
        private readonly WorkflowThread _parent;
        private readonly string _eventName;
        private Type _payloadType;

        public ThrowEventNodeOutput(string eventName, WorkflowThread parent)
        {
            _eventName = eventName;
            _parent = parent;
        }

        public void RegisteredAs(Type payloadType)
        {
            _payloadType = payloadType;
            _parent.RegisterWorkflowEventDef(this);
        }

        public PutWorkflowEventDefRequest ToPutWorkflowEventDefRequest()
        {
            if (_payloadType == null)
            {
                throw new InvalidOperationException($"Payload type for event '{_eventName}' was not set. Did you forget to call RegisteredAs?");
            }
            return new PutWorkflowEventDefRequest
            {
                Name = _eventName,
                ContentType = TypeUtil.DotNetTypeToReturnType(_payloadType)
            };
        }
    }
}
