using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;

namespace LittleHorse.Sdk.Helper
{
    /// <summary>
    /// Represents a native LittleHorse array type.
    /// </summary>
    public class LHArrayType : LHClassType
    {
        private readonly LHClassType _componentType;

        /// <summary>
        /// Creates an LH array wrapper from a .NET array type.
        /// </summary>
        /// <param name="type">A .NET array type (for example, typeof(string[])).</param>
        public LHArrayType(Type type) : base(type)
        {
            if (!type.IsArray)
            {
                throw new ArgumentException($"LHArrayType can only be created from array types; got {type.Name}.");
            }

            if (type == typeof(byte[]))
            {
                throw new ArgumentException("byte[] is BYTES in LittleHorse and cannot be used as a native LH Array type.");
            }

            Type? componentClass = type.GetElementType();
            if (componentClass == null)
            {
                throw new ArgumentException($"Unable to resolve array component type for {type.Name}.");
            }

            _componentType = componentClass.IsArray
                ? new LHArrayType(componentClass)
                : LHClassType.FromType(componentClass);

            try
            {
                LHTypeConstraintValidator.EnsureNoJsonPrimitiveTypes(_componentType.GetTypeDefinition());
            }
            catch (ForbiddenJsonTypeException ex)
            {
                throw new ArgumentException(
                    $"Native LH Array component type cannot contain {ex.ForbiddenType}. Use strongly typed Struct/Array definitions.",
                    ex);
            }
        }

        /// <inheritdoc />
        public override TypeDefinition.DefinedTypeOneofCase GetDefinedTypeCase()
        {
            return TypeDefinition.DefinedTypeOneofCase.InlineArrayDef;
        }

        /// <inheritdoc />
        public override TypeDefinition GetTypeDefinition()
        {
            return new TypeDefinition
            {
                InlineArrayDef = new InlineArrayDef
                {
                    ArrayType = _componentType.GetTypeDefinition()
                }
            };
        }
    }
}
