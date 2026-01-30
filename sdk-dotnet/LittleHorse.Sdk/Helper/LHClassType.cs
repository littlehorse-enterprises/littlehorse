namespace LittleHorse.Sdk.Helper
{
  /// <summary>
  /// 
  /// </summary>
  public abstract class LHClassType
  {
    /// <summary>
    /// 
    /// </summary>
    protected Type _classType;

    /// <summary>
    /// 
    /// </summary>
    protected LHClassType(Type type)
    {
      _classType = type;
    }

    /// <summary>
    /// 
    /// </summary>
    /// <param name="type"></param>
    /// <returns></returns>
    public static LHClassType FromType(Type type)
    {
      if (type == null)
      {
        throw new Exception();
      } else if (Attribute.IsDefined(type, typeof(LHStructDefAttribute)))
      {
        return new LHStructDefType(type);
      }
      return new LHPrimitiveType(type);
    }

    /// <summary>
    /// 
    /// </summary>
    public object? CreateInstance()
    {
      return Activator.CreateInstance(_classType);
    }

    /// <summary>
    /// 
    /// </summary>
    /// <returns></returns>
    public abstract Common.Proto.TypeDefinition.DefinedTypeOneofCase GetDefinedTypeCase();

    /// <summary>
    /// 
    /// </summary>
    /// <returns></returns>
    public abstract Common.Proto.TypeDefinition GetTypeDefinition();
  }
}