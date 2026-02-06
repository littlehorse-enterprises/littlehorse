/// <summary>
/// Marks a class as a LittleHorse struct definition and provides its name.
/// </summary>
[AttributeUsage(AttributeTargets.Class)]
public class LHStructDefAttribute : Attribute
{
  /// <summary>
  /// The struct definition name.
  /// </summary>
  public string Value;

  /// <summary>
  /// Creates a struct definition attribute.
  /// </summary>
  /// <param name="value">The struct definition name.</param>
  public LHStructDefAttribute(string value)
  {
    Value = value;
  }
}