using System;

namespace LittleHorse.Sdk.Worker
{
  /// <summary>
  /// Marks a class as a LittleHorse StructDef, providing its name and description.
  /// </summary>
  [AttributeUsage(AttributeTargets.Class)]
  public class LHStructDefAttribute : Attribute
  {
    /// <summary>
    /// The StructDef name.
    /// </summary>
    public string Value;

    /// <summary>
    /// The StructDef description.
    /// </summary>
    public string Description;

    /// <summary>
    /// Creates a StructDef attribute.
    /// </summary>
    /// <param name="value">The StructDef name.</param>
    /// <param name="description">The StructDef description.</param>
    public LHStructDefAttribute(string value, string description = "")
    {
      Value = value;
      Description = description;
    }
  }
}