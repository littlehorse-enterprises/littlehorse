using System.Reflection;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Worker
{
    /// <summary>
    /// Metadata extracted from <see cref="LHTypeAttribute"/> on methods and parameters.
    /// </summary>
    internal sealed class LHTypeMetadata
    {
        internal enum ValidationContext
        {
            Parameter,
            ReturnType
        }

        internal bool Masked { get; }
        internal string Name { get; }
        internal bool IsLHArray { get; }

        private LHTypeMetadata(bool masked, string name, bool isLHArray)
        {
            Masked = masked;
            Name = name;
            IsLHArray = isLHArray;
        }

        internal static LHTypeMetadata From(ParameterInfo parameter)
        {
            if (parameter.GetCustomAttribute(typeof(LHTypeAttribute)) is not LHTypeAttribute lhType)
            {
                return new LHTypeMetadata(masked: false, name: string.Empty, isLHArray: false);
            }

            return new LHTypeMetadata(lhType.Masked, lhType.Name, lhType.IsLHArray);
        }

        internal static LHTypeMetadata From(MethodInfo method)
        {
            if (method.GetCustomAttribute(typeof(LHTypeAttribute)) is not LHTypeAttribute lhType)
            {
                return new LHTypeMetadata(masked: false, name: string.Empty, isLHArray: false);
            }

            return new LHTypeMetadata(lhType.Masked, lhType.Name, lhType.IsLHArray);
        }

        internal static LHClassType ResolveTaskDefinitionType(Type type, bool isLHArray)
        {
            if (!isLHArray)
            {
                return LHClassType.FromType(type);
            }

            if (type == typeof(byte[]))
            {
                throw new LHTaskSchemaMismatchException("Cannot use IsLHArray=true with byte[]. byte[] is BYTES.");
            }

            if (type.IsArray)
            {
                return new LHArrayType(type);
            }

            if (LHMappingHelper.TryGetListElementType(type, out Type elementType))
            {
                return new LHArrayType(elementType.MakeArrayType());
            }

            throw new LHTaskSchemaMismatchException(
                $"IsLHArray=true requires an array or IList<T> type, but got {type.Name}.");
        }

        internal void ValidateLHArrayUsage(Type dotNetType, ValidationContext context, string contextName)
        {
            if (!IsLHArray)
            {
                return;
            }

            if (dotNetType == typeof(byte[]))
            {
                throw new LHTaskSchemaMismatchException(
                    "Cannot use @LHType(isLHArray = true) with byte[]. byte[] maps to BYTES in LittleHorse.");
            }

            if (!dotNetType.IsArray && !LHMappingHelper.TryGetListElementType(dotNetType, out _))
            {
                throw new LHTaskSchemaMismatchException(BuildUnexpectedLHArrayMessage(context, contextName, dotNetType));
            }
        }

        private static string BuildUnexpectedLHArrayMessage(ValidationContext context, string contextName, Type dotNetType)
        {
            if (context == ValidationContext.Parameter)
            {
                return "@LHType(isLHArray = true) can only be used on array or IList<T> parameters. Invalid parameter "
                    + contextName
                    + " with .NET type "
                    + dotNetType.FullName
                    + ".";
            }

            return "@LHType(isLHArray = true) can only be used on array or IList<T> return types. Invalid return type for method "
                + contextName
                + " with .NET type "
                + dotNetType.FullName
                + ".";
        }
    }
}