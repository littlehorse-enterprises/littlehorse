using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

/// <summary>
/// 
/// </summary>
public class LHPrimitiveType : LHClassType
{
  /// <summary>
  /// 
  /// </summary>
  /// <param name="type"></param>
  public LHPrimitiveType(Type type) : base(type)
  {
    
  }

  /// <summary>
  /// 
  /// </summary>
  /// <returns></returns>
  public override TypeDefinition.DefinedTypeOneofCase GetDefinedTypeCase()
  {
    return TypeDefinition.DefinedTypeOneofCase.PrimitiveType;
  }

  /// <summary>
  /// 
  /// </summary>
  /// <returns></returns>
  /// <exception cref="NotImplementedException"></exception>
  public override TypeDefinition GetTypeDefinition()
  {
    return new TypeDefinition
    {
      PrimitiveType = LHMappingHelper.DotNetTypeToLHVariableType(_classType)
    };
  }
}