using System.ComponentModel;

public class LHStructProperty
{
  PropertyDescriptor _pd;

  /// <summary>
  /// 
  /// </summary>
  /// <param name="pd"></param>
  /// <param name="parentStructDef"></param>
  public LHStructProperty(PropertyDescriptor pd, LHStructDefType parentStructDef)
  {
    _pd = ArgumentNullException.ThrowIfNull(pd);
  }
}