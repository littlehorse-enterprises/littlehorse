namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// The `WaitForConditionNodeOutput` class represents a specialized NodeOutput
/// used to handle outputs when a wait for condition node is triggered.
/// 
/// </summary>
public class WaitForConditionNodeOutput: NodeOutput
{
    public WaitForConditionNodeOutput(string nodeName, WorkflowThread parent) : base(nodeName, parent)
    {
    }
}
