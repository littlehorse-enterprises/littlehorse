using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Helper
{
  /// <summary>
  /// Represents a StructDef reference by id (name + optional version).
  /// </summary>
  public class LHStructDefId : LHClassType
  {
    private readonly string _structDefName;
    private readonly int _version;

    /// <summary>
    /// Creates a StructDef id that resolves to the latest version.
    /// </summary>
    /// <param name="structDefName">The StructDef name.</param>
    public LHStructDefId(string structDefName) : this(structDefName, -1)
    {
    }

    /// <summary>
    /// Creates a StructDef id with an explicit version.
    /// </summary>
    /// <param name="structDefName">The StructDef name.</param>
    /// <param name="version">The StructDef version.</param>
    public LHStructDefId(string structDefName, int version) : base(typeof(object))
    {
      _structDefName = structDefName;
      _version = version;
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
    /// Gets the LittleHorse <see cref="TypeDefinition"/> for this struct definition id.
    /// </summary>
    /// <returns>The type definition.</returns>
    public override TypeDefinition GetTypeDefinition()
    {
      return new TypeDefinition
      {
        StructDefId = new StructDefId
        {
          Name = _structDefName,
          Version = _version
        }
      };
    }
  }
}