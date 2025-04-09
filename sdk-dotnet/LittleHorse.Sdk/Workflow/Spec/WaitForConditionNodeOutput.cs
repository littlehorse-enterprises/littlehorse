namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// The `WaitForConditionNodeOutput` class represents a specialized NodeOutput
/// used to handle outputs when a wait for condition node is triggered.
/// 
/// </summary>
public class WaitForConditionNodeOutput: NodeOutput
{
    /// <summary>
    /// Initializes a new instance of the <see cref="WaitForConditionNodeOutput"/> class.
    /// </summary>
    /// <param name="nodeName">The specified node name.</param>
    /// <param name="parent">The workflow thread where the wait for WaitForConditionNodeOutput belongs to.</param>
    public WaitForConditionNodeOutput(string nodeName, WorkflowThread parent) : base(nodeName, parent)
    {
    }
}
