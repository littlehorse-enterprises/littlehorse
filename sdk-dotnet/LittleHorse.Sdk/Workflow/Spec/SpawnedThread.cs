using LittleHorse.Sdk.Common.Proto;
using static LittleHorse.Sdk.Common.Proto.WaitForThreadsNode.Types;

namespace LittleHorse.Sdk.Workflow.Spec;

public class SpawnedThread
{
    public WorkflowThread Parent { get; private set; }
    public WfRunVariable ThreadNumberVariable { get; private set; }
    public String ChildThreadName { get; private set; }

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