using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Exceptions
{
    /// <summary>
    /// Thrown when a type definition includes forbidden JSON primitives in a native array context.
    /// </summary>
    public class ForbiddenJsonTypeException : Exception
    {
        /// <summary>
        /// The forbidden variable type that was found.
        /// </summary>
        public VariableType ForbiddenType { get; }

        /// <summary>
        /// Creates a new instance of the exception.
        /// </summary>
        /// <param name="forbiddenType">The forbidden variable type.</param>
        public ForbiddenJsonTypeException(VariableType forbiddenType)
            : base($"Forbidden type [{forbiddenType}] in StructDef or InlineArrayDef. Use Struct/Array typing instead of JSON_OBJ/JSON_ARR.")
        {
            ForbiddenType = forbiddenType;
        }
    }
}
