using LittleHorse.Common.Proto;

namespace LittleHorse.Sdk.Worker.Internal;

internal class LHMethodParam
{
    private VariableType _type;
    private string? _name;
    private bool _isMasked;
    
    internal VariableType Type
    {
        get => _type;
        set => _type = value;
    }
    
    public string? Name
    {
        get => _name;
        set => _name = value;
    }

    public bool IsMasked
    {
        get => _isMasked;
        set => _isMasked = value;
    }
}