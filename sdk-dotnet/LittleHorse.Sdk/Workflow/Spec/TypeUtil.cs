using System.Collections;
using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec
{
    /// <summary>
    /// Utility methods for mapping .NET types to workflow return types.
    /// </summary>
    public static class TypeUtil
    {
        /// <summary>
        /// Maps a .NET <see cref="Type"/> to a workflow <see cref="ReturnType"/>.
        /// </summary>
        /// <param name="type">The .NET type to map.</param>
        /// <returns>The corresponding workflow <see cref="ReturnType"/>.</returns>
        /// <exception cref="ArgumentException">Thrown if the type is not supported.</exception>
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
