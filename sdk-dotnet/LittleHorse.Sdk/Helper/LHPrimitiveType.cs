using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Helper
{
  /// <summary>
  /// Represents a LittleHorse primitive type backed by a .NET type.
  /// </summary>
  public class LHPrimitiveType : LHClassType
  {
    /// <summary>
    /// Creates a primitive type wrapper.
    /// </summary>
    /// <param name="type">The underlying .NET type.</param>
    public LHPrimitiveType(Type type) : base(type)
    {
      
    }

    /// <summary>
    /// Gets the defined type case for primitive types.
    /// </summary>
    /// <returns>The defined type case.</returns>
    public override TypeDefinition.DefinedTypeOneofCase GetDefinedTypeCase()
    {
      return TypeDefinition.DefinedTypeOneofCase.PrimitiveType;
    }

    /// <summary>
    /// Gets the LittleHorse <see cref="TypeDefinition"/> for this primitive type.
    /// </summary>
    /// <returns>The type definition.</returns>
    public override TypeDefinition GetTypeDefinition()
    {
      return new TypeDefinition
      {
        PrimitiveType = LHMappingHelper.DotNetTypeToLHVariableType(_classType)
      };
    }
  }
}