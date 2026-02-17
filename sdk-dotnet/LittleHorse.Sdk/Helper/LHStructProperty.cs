using System.ComponentModel;
using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Worker;

/// <summary>
/// Represents a single struct property and its LittleHorse metadata.
/// </summary>
public class LHStructProperty
{
  private readonly PropertyDescriptor _pd;
  private readonly LHStructDefType _parentStructDef;
  private readonly PropertyInfo? _propertyInfo;
  private readonly MethodInfo? _getter;
  private readonly MethodInfo? _setter;

  /// <summary>
  /// The LittleHorse field name for this property.
  /// </summary>
  public string FieldName { get; }

  /// <summary>
  /// Whether this field is marked as masked.
  /// </summary>
  public bool Masked { get; }

  /// <summary>
  /// Whether this field should be ignored for struct definitions.
  /// </summary>
  public bool Ignored { get; }

  /// <summary>
  /// Creates a property wrapper for a struct definition.
  /// </summary>
  /// <param name="pd">The property descriptor.</param>
  /// <param name="parentStructDef">The parent struct definition type.</param>
  public LHStructProperty(PropertyDescriptor pd, LHStructDefType parentStructDef)
  {
    ArgumentNullException.ThrowIfNull(pd);
    ArgumentNullException.ThrowIfNull(parentStructDef);
    _pd = pd;
    _parentStructDef = parentStructDef;

    _propertyInfo = _pd.ComponentType.GetProperty(_pd.Name);
    _getter = _propertyInfo?.GetGetMethod(true);
    _setter = _propertyInfo?.GetSetMethod(true);

    FieldName = FindFieldName();
    Masked = FindIsMasked();
    Ignored = FindIsIgnored();
  }

  /// <summary>
  /// Reads the property value from the given object and converts it to a <see cref="VariableValue"/>.
  /// </summary>
  /// <param name="o">The source object.</param>
  /// <returns>The converted variable value, or null if the property value is null.</returns>
  public VariableValue? GetValueFrom(object o)
  {
    if (!HasReadMethod())
    {
      throw new InvalidOperationException(
          "No read method for property " + FieldName + " found on object of type: " + o.GetType());
    }

    try
    {
      object? val = _pd.GetValue(o);
      if (val == null) return null;
      return LHMappingHelper.ObjectToVariableValue(val);
    }
    catch (Exception ex)
    {
      throw new LHSerdeException(
          "Failed getting value of property " + FieldName + " from object of type: " + o.GetType(),
          ex);
    }
  }

  /// <summary>
  /// Sets the property value on the given object using a <see cref="VariableValue"/>.
  /// </summary>
  /// <param name="o">The target object.</param>
  /// <param name="v">The variable value to apply.</param>
  public void SetValueTo(object o, VariableValue v)
  {
    if (!HasWriteMethod())
    {
      throw new InvalidOperationException(string.Format(
          "No write method for property [{0}] found on object of type [{1}]",
          FieldName,
          o.GetType()));
    }

    try
    {
      object? converted = LHMappingHelper.VariableValueToObject(v, _pd.PropertyType);
      _pd.SetValue(o, converted);
    }
    catch (Exception ex)
    {
      throw new LHSerdeException(
          string.Format(
              "Failed setting value of property [{0}] from object of type", FieldName, o.GetType()),
          ex);
    }
  }

  /// <summary>
  /// Converts this property to a <see cref="StructFieldDef"/>.
  /// </summary>
  public StructFieldDef ToStructFieldDef()
  {
    LHClassType propertyClass = GetPropertyType();
    TypeDefinition typeDef = propertyClass.GetTypeDefinition();
    typeDef.Masked = Masked;

    var fieldDef = new StructFieldDef
    {
      FieldType = typeDef
    };

    VariableValue? defaultValue = GetDefaultValue();
    if (defaultValue != null)
    {
      fieldDef.DefaultValue = defaultValue;
    }

    return fieldDef;
  }

  /// <summary>
  /// Gets the default value for this property from a default instance, if available.
  /// </summary>
  public VariableValue? GetDefaultValue()
  {
    try
    {
      object? defaultInstance = _parentStructDef.CreateInstance();
      if (defaultInstance == null)
      {
        return null;
      }

      return GetValueFrom(defaultInstance);
    }
    catch
    {
      return null;
    }
  }

  /// <summary>
  /// Gets the <see cref="LHClassType"/> for this property.
  /// </summary>
  public LHClassType GetPropertyType()
  {
    return LHClassType.FromType(_pd.PropertyType);
  }

  private T? GetAnnotation<T>() where T : Attribute
  {
    if (_propertyInfo != null)
    {
      T? attr = _propertyInfo.GetCustomAttribute<T>(true);
      if (attr != null) return attr;

      if (_getter != null)
      {
        attr = _getter.GetCustomAttribute<T>(true);
        if (attr != null) return attr;
      }

      if (_setter != null)
      {
        attr = _setter.GetCustomAttribute<T>(true);
        if (attr != null) return attr;
      }
    }

    return _pd.Attributes[typeof(T)] as T;
  }

  private bool FindIsIgnored()
  {
    return GetAnnotation<LHStructIgnoreAttribute>() != null;
  }

  private bool FindIsMasked()
  {
    var lhStructField = GetAnnotation<LHStructFieldAttribute>();
    if (lhStructField == null) return false;

    return lhStructField.Masked;
  }

  private string FindFieldName()
  {
    var lhStructField = GetAnnotation<LHStructFieldAttribute>();
    string fieldName;
    if (lhStructField == null || string.IsNullOrWhiteSpace(lhStructField.Name))
    {
      fieldName = _pd.Name;
    }
    else
    {
      fieldName = lhStructField.Name;
    }

    return LowercaseFirstLetter(fieldName);
  }

  private static string LowercaseFirstLetter(string value)
  {
    if (string.IsNullOrEmpty(value)) return value;
    if (char.IsLower(value[0])) return value;

    return char.ToLowerInvariant(value[0]) + value.Substring(1);
  }

  private bool HasReadMethod()
  {
    return _getter != null || !_pd.IsReadOnly;
  }

  private bool HasWriteMethod()
  {
    return _setter != null && !_pd.IsReadOnly;
  }

  /// <summary>
  /// Returns a debug-friendly representation of this property.
  /// </summary>
  public override string ToString()
  {
    return _parentStructDef + ": " + FieldName;
  }
}