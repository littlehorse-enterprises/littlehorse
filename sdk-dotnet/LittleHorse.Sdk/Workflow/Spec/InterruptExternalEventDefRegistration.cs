using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Workflow.Spec;

internal sealed class InterruptExternalEventDefRegistration : IExternalEventDefRegistration
{
    private readonly string _externalEventDefName;
    private readonly Type? _payloadType;

    public InterruptExternalEventDefRegistration(string externalEventDefName, Type? payloadType)
    {
        _externalEventDefName = externalEventDefName;
        _payloadType = payloadType;
    }

    public PutExternalEventDefRequest ToPutExternalEventDefRequest()
    {
        var req = new PutExternalEventDefRequest
        {
            Name = _externalEventDefName
        };

        if (_payloadType != null)
        {
            req.ContentType = LHMappingHelper.DotNetTypeToReturnType(_payloadType);
        }

        return req;
    }
}
