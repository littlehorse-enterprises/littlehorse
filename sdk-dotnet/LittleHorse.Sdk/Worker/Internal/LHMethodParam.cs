using System.Runtime.CompilerServices;
using LittleHorse.Sdk.Common.Proto;

[assembly: InternalsVisibleTo("LittleHorse.Sdk.Tests")]

namespace LittleHorse.Sdk.Worker.Internal
{
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
    
        internal string? Name
        {
            get => _name;
            set => _name = value;
        }

        internal bool IsMasked
        {
            get => _isMasked;
            set => _isMasked = value;
        }
    }
}