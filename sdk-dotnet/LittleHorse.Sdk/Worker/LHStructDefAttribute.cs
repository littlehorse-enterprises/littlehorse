/// <summary>
/// 
/// </summary>
[AttributeUsage(AttributeTargets.Class)]
public class LHStructDefAttribute : Attribute
{
  /// <summary>
  /// 
  /// </summary>
  public string Value;

  /// <summary>
  /// 
  /// </summary>
  /// <param name="value"></param>
  public LHStructDefAttribute(string value)
  {
    Value = value;
  }
}