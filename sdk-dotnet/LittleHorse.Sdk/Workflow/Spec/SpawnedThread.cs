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
}