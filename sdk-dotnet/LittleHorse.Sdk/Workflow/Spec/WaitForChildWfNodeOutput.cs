using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// 
/// </summary>
public class WaitForChildWfNodeOutput : NodeOutput
{
  /// <summary>
  /// 
  /// </summary>
  /// <param name="nodeName"></param>
  /// <param name="parent"></param>
  public WaitForChildWfNodeOutput(string nodeName, WorkflowThread parent) : base(nodeName, parent)
  {
  }
}