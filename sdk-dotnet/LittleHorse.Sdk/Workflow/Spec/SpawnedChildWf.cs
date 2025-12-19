using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a spawned child workflow from a `runWf` method.
/// </summary>
public class SpawnedChildWf
{
    private string sourceNodeName;
    private WorkflowThread thread;

    /// <summary>
    /// 
    /// </summary>
    /// <param name="sourceNodeName"></param>
    /// <param name="thread"></param>
    public SpawnedChildWf(string sourceNodeName, WorkflowThread thread)
    {
        this.sourceNodeName = sourceNodeName;
        this.thread = thread;
    }

    /// <summary>
    /// 
    /// </summary>
    public string SourceNodeName
    {
        get { return sourceNodeName; }
    }

    /// <summary>
    /// 
    /// </summary>
    public WorkflowThread Thread
    {
        get { return thread; }
    }

    /// <summary>
    /// 
    /// </summary>
    /// <returns></returns>
    public WaitForChildWfNode BuildNode()
    {
        WaitForChildWfNode node = new WaitForChildWfNode
        {
        ChildWfRunId = new VariableAssignment
        {
            NodeOutput = new VariableAssignment.Types.NodeOutputReference
            {
            NodeName = sourceNodeName
            }
        },
        ChildWfRunSourceNode = sourceNodeName
        };

        return node;
    }
}