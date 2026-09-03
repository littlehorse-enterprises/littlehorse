using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a spawned child workflow from a `RunWf` method.
/// </summary>
public class SpawnedChildWf
{
    internal string sourceNodeName;
    internal WorkflowThread thread;
    private readonly RunChildWfNode runChildWfNode;

    /// <summary>
    /// Initializes a new instance of the <see cref="SpawnedChildWf"/> class with the specified node name
    /// and the parent WorkflowThread. 
    /// </summary>
    /// <param name="sourceNodeName"></param>
    /// <param name="thread"></param>
    /// <param name="runChildWfNode"></param>
    public SpawnedChildWf(string sourceNodeName, WorkflowThread thread, RunChildWfNode runChildWfNode)
    {
        this.sourceNodeName = sourceNodeName;
        this.thread = thread;
        this.runChildWfNode = runChildWfNode;
    }

    /// <summary>
    /// Sets the ID assigned to the child WfRun.
    /// </summary>
    /// <param name="childId">A literal, workflow variable, or expression resolving to the child ID.</param>
    /// <returns>This child workflow handle.</returns>
    public SpawnedChildWf WithChildId(object childId)
    {
        runChildWfNode.ChildId = thread.AssignVariableHelper(childId);
        return this;
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
