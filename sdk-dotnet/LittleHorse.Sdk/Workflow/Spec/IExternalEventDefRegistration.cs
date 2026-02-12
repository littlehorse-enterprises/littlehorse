using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

internal interface IExternalEventDefRegistration
{
    PutExternalEventDefRequest ToPutExternalEventDefRequest();
}
