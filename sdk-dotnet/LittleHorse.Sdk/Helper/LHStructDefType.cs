using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Worker;

namespace LittleHorse.Sdk.Helper
{
  /// <summary>
  /// Represents a struct definition type based on a class annotated with <see cref="LHStructDefAttribute"/>.
  /// </summary>
  public class LHStructDefType : LHClassType
  {
    private readonly InlineStructDef _inlineStructDef;
    private List<LHStructProperty>? _structProperties;
    /// <summary>
    /// Creates a struct definition wrapper for the provided type.
    /// </summary>
    /// <param name="type">The annotated struct definition type.</param>
    public LHStructDefType(Type type) : base(type)
    {
      if (!Attribute.IsDefined(_classType, typeof(LHStructDefAttribute)))
      {
        throw new ArgumentException("Cannot create LHStructDefType. Missing `LHStructDef` attribute on provided type: " + _classType.Name);
      }

      _inlineStructDef = BuildInlineStructDef();
    }

    /// <summary>
    /// Gets the defined type discriminator for struct definitions.
    /// </summary>
    /// <returns>The defined type case for struct definitions.</returns>
    public override TypeDefinition.DefinedTypeOneofCase GetDefinedTypeCase()
    {
      return TypeDefinition.DefinedTypeOneofCase.StructDefId;
    }

    /// <summary>
    /// Gets the <see cref="LHStructDefAttribute"/> for the underlying type.
    /// </summary>
    /// <returns>The struct definition attribute.</returns>
    private LHStructDefAttribute GetStructDefAnnotation()
    {
      var attr = Attribute.GetCustomAttribute(_classType, typeof(LHStructDefAttribute)) as LHStructDefAttribute;
      if (attr == null)
      {
        throw new InvalidOperationException("Missing `LHStructDef` attribute on type: " + _classType.Name);
      }

      return attr;
    }

    /// <summary>
    /// Gets the LittleHorse <see cref="TypeDefinition"/> for this struct definition.
    /// </summary>
    /// <returns>The type definition.</returns>
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

    /// <summary>
    /// Gets the <see cref="StructDefId"/> for this struct definition.
    /// </summary>
    public StructDefId GetStructDefId()
    {
      return new StructDefId
      {
        Name = GetStructDefAnnotation().Value
      };
    }

    /// <summary>
    /// Gets the description for this StructDef from its annotation.
    /// </summary>
    /// <returns>The description for this StructDef</returns>
    public string GetStructDefDescription()
    {
      return GetStructDefAnnotation().Description;
    }

    /// <summary>
    /// Gets the <see cref="InlineStructDef"/> describing the fields of this struct.
    /// </summary>
    public InlineStructDef GetInlineStructDef()
    {
      return _inlineStructDef;
    }

    /// <summary>
    /// Gets the list of <see cref="LHStructProperty"/> instances for this struct.
    /// </summary>
    public IList<LHStructProperty> GetStructProperties()
    {
      if (_structProperties == null)
      {
        _structProperties = BuildStructProperties();
      }

      return _structProperties;
    }

    private List<LHStructProperty> BuildStructProperties()
    {
      return TypeDescriptor.GetProperties(_classType)
        .Cast<PropertyDescriptor>()
        .Select(pd => new LHStructProperty(pd, this))
        .Where(property => !property.Ignored)
        .ToList();
    }

    private InlineStructDef BuildInlineStructDef()
    {
      var inlineStructDef = new InlineStructDef();

      foreach (LHStructProperty property in GetStructProperties())
      {
        inlineStructDef.Fields.Add(property.FieldName, property.ToStructFieldDef());
      }

      return inlineStructDef;
    }
  }
}