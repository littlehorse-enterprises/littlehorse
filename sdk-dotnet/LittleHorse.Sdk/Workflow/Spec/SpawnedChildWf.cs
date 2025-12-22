using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a spawned child workflow from a `RunWf` method.
/// </summary>
public class SpawnedChildWf
{
    internal string sourceNodeName;
    internal WorkflowThread thread;

    /// <summary>
    /// Initializes a new instance of the <see cref="SpawnedChildWf"/> class with the specified node name
    /// and the parent WorkflowThread. 
    /// </summary>
    /// <param name="sourceNodeName"></param>
    /// <param name="thread"></param>
    public SpawnedChildWf(string sourceNodeName, WorkflowThread thread)
    {
        this.sourceNodeName = sourceNodeName;
        this.thread = thread;
    }

    /// <summary>
    /// The node name 
    /// </summary>
    internal string SourceNodeName
    {
        get { return sourceNodeName; }
    }

    /// <summary>
    /// 
    /// </summary>
    internal WorkflowThread Thread
    {
        get { return thread; }
    }

    /// <summary>
    /// 
    /// </summary>
    /// <returns></returns>
    internal WaitForChildWfNode BuildNode()
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