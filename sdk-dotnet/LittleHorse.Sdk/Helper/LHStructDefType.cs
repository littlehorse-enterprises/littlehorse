using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;


/// <summary>
/// 
/// </summary>
public class LHStructDefType : LHClassType
{
  InlineStructDef inlineStructDef;
  /// <summary>
  /// 
  /// </summary>
  /// <param name="type"></param>
  public LHStructDefType(Type type) : base(type)
  {
    if (!Attribute.IsDefined(_classType, typeof(LHStructDefAttribute)))
    {
      throw new ArgumentException("Cannot create LHStructDefType. Missing `LHStructDef` attribute on provided type: " + _classType.Name);
    }

    inlineStructDef = 
  }

  /// <summary>
  /// 
  /// </summary>
  /// <returns></returns>
  public override TypeDefinition.DefinedTypeOneofCase GetDefinedTypeCase()
  {
    return TypeDefinition.DefinedTypeOneofCase.StructDefId;
  }

  /// <summary>
  /// 
  /// </summary>
  /// <returns></returns>
  private LHStructDefAttribute GetStructDefAnnotation()
  {
    return (LHStructDefAttribute) Attribute.GetCustomAttribute(_classType, typeof(LHStructDefAttribute));
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
      StructDefId = new StructDefId
      {
        Name = GetStructDefAnnotation().Value
      }
    };
  }

  private 

  private InlineStructDef BuildInlineStructDef()
  {
    // InlineStructDef inlineStructDef = new InlineStructDef(
  }
}