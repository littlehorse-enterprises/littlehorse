using LittleHorse.Sdk.Common.Proto;
using static LittleHorse.Sdk.Common.Proto.WaitForThreadsNode.Types;

namespace LittleHorse.Sdk.Workflow.Spec;

public class SpawnedThread
{
    public WorkflowThread Parent { get; private set; }
    private readonly WfRunVariable _internalThreadVar;
    public String ChildThreadName { get; private set; }

    public SpawnedThread(WorkflowThread parent, string childThreadName, WfRunVariable internalThreadVar) 
    {
        Parent = parent;
        ChildThreadName = childThreadName;
        _internalThreadVar = internalThreadVar;
    }
    
    public WfRunVariable GetThreadNumberVariable() 
    {
        return _internalThreadVar;
    }

    internal ThreadToWaitFor BuildThreadToWaitFor()
    {
        if (_internalThreadVar.Type != VariableType.Int) 
        {
            throw new ArgumentException("Only int variables are supported.");
        }
        
        var variableAssignment = Parent.AssignVariableHelper(_internalThreadVar);
        var threadToWaitFor = new ThreadToWaitFor
        {
            ThreadRunNumber = variableAssignment
        };

        return threadToWaitFor;
    }
}