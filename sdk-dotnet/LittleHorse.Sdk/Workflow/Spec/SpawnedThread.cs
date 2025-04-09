using LittleHorse.Sdk.Common.Proto;
using static LittleHorse.Sdk.Common.Proto.WaitForThreadsNode.Types;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// This is the output of <c>WorkflowThread.SpawnThread(...);</c>. It is used as input for
/// <c>WorkflowThread.WaitForThread(...);</c>.
/// </summary>
public class SpawnedThread
{
    /// <value>
    /// The workflow thread that this output belongs to.
    /// </value>
    public WorkflowThread Parent { get; private set; }
    
    /// <value>
    /// The thread number variable in the parent thread. This is used to identify the thread to wait for.
    /// </value>
    public WfRunVariable ThreadNumberVariable { get; private set; }
    
    /// <value>
    /// The name of the child thread. This is used to identify the thread in the workflow
    /// </value>
    public string ChildThreadName { get; private set; }

    /// <summary>
    /// Initializes a new instance of the <see cref="SpawnedThread"/> class with the specified parent workflow thread,
    /// child thread name, and thread number variable.
    /// </summary>
    /// <param name="parent">The parent workflow thread.</param>
    /// <param name="childThreadName">The name of the child thread.</param>
    /// <param name="threadNumberVariable">Any <c>wfRunVariable</c> in the thread.</param>
    public SpawnedThread(WorkflowThread parent, string childThreadName, WfRunVariable threadNumberVariable) 
    {
        Parent = parent;
        ChildThreadName = childThreadName;
        ThreadNumberVariable = threadNumberVariable;
    }

    internal ThreadToWaitFor BuildThreadToWaitFor()
    {
        if (ThreadNumberVariable.Type != VariableType.Int) 
        {
            throw new ArgumentException("Only int variables are supported.");
        }
        
        var variableAssignment = Parent.AssignVariableHelper(ThreadNumberVariable);
        var threadToWaitFor = new ThreadToWaitFor
        {
            ThreadRunNumber = variableAssignment
        };

        return threadToWaitFor;
    }
}