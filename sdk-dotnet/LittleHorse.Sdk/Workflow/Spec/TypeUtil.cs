using System.Collections;
using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec
{
    public static class TypeUtil
    {
        public static ReturnType DotNetTypeToReturnType(Type type)
        {
            var typeDef = new TypeDefinition();
            if (type == typeof(string))
                typeDef.Type = VariableType.Str;
            else if (type == typeof(int))
                typeDef.Type = VariableType.Int;
            else if (type == typeof(double))
                typeDef.Type = VariableType.Double;
            else if (type == typeof(bool))
                typeDef.Type = VariableType.Bool;
            else if (typeof(IDictionary).IsAssignableFrom(type))
                typeDef.Type = VariableType.JsonObj;
            else if (typeof(IEnumerable).IsAssignableFrom(type) && type != typeof(string))
                typeDef.Type = VariableType.JsonArr;
            else
                throw new ArgumentException("Unsupported payload type for workflow event.");
            return new ReturnType { ReturnType_ = typeDef };
        }
    }
}
