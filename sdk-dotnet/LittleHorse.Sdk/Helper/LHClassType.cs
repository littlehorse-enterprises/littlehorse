namespace LittleHorse.Sdk.Helper
{
  /// <summary>
  /// Base class for describing LittleHorse types backed by .NET types.
  /// </summary>
  public abstract class LHClassType
  {
    /// <summary>
    /// The underlying .NET type.
    /// </summary>
    protected Type _classType;

    /// <summary>
    /// Creates a new class type wrapper.
    /// </summary>
    /// <param name="type">The underlying .NET type.</param>
    protected LHClassType(Type type)
    {
      _classType = type;
    }

    /// <summary>
    /// Creates an <see cref="LHClassType"/> for the provided .NET type.
    /// </summary>
    /// <param name="type">The .NET type.</param>
    /// <returns>The corresponding class type wrapper.</returns>
    public static LHClassType FromType(Type type)
    {
      if (type == null)
      {
        throw new ArgumentNullException(nameof(type), "Type cannot be null.");
      } else if (Attribute.IsDefined(type, typeof(LHStructDefAttribute)))
      {
        return new LHStructDefType(type);
      }
      return new LHPrimitiveType(type);
    }

    /// <summary>
    /// Creates a new instance of the underlying .NET type.
    /// </summary>
    public object? CreateInstance()
    {
      return Activator.CreateInstance(_classType);
    }

    /// <summary>
    /// Gets the defined type case for this class type.
    /// </summary>
    /// <returns>The defined type case.</returns>
    public abstract Common.Proto.TypeDefinition.DefinedTypeOneofCase GetDefinedTypeCase();

    /// <summary>
    /// Gets the LittleHorse <see cref="Common.Proto.TypeDefinition"/> for this class type.
    /// </summary>
    /// <returns>The type definition.</returns>
    public abstract Common.Proto.TypeDefinition GetTypeDefinition();
  }
}