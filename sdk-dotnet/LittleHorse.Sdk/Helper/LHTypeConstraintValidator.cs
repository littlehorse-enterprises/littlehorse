using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;

namespace LittleHorse.Sdk.Helper
{
    internal static class LHTypeConstraintValidator
    {
        internal static void EnsureNoJsonPrimitiveTypes(TypeDefinition typeDefinition)
        {
            VariableType? forbidden = FindForbiddenJsonPrimitive(typeDefinition);
            if (forbidden.HasValue)
            {
                throw new ForbiddenJsonTypeException(forbidden.Value);
            }
        }

        private static VariableType? FindForbiddenJsonPrimitive(TypeDefinition typeDefinition)
        {
            switch (typeDefinition.DefinedTypeCase)
            {
                case TypeDefinition.DefinedTypeOneofCase.PrimitiveType:
                    if (typeDefinition.PrimitiveType == VariableType.JsonObj
                        || typeDefinition.PrimitiveType == VariableType.JsonArr)
                    {
                        return typeDefinition.PrimitiveType;
                    }

                    return null;

                case TypeDefinition.DefinedTypeOneofCase.InlineArrayDef:
                    return FindForbiddenJsonPrimitive(typeDefinition.InlineArrayDef.ArrayType);

                default:
                    return null;
            }
        }
    }
}
