using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class SpawnedThreadsIterator: SpawnedThreads
{
    private readonly WfRunVariable _internalStartedThreadVar;

    public SpawnedThreadsIterator(WfRunVariable internalStartedThreadVar) 
    {
       _internalStartedThreadVar = internalStartedThreadVar;
        if (_internalStartedThreadVar.Type != VariableType.JsonArr) 
        {
            throw new ArgumentException("Only support for json arrays.");
        }
    }
    
    public WaitForThreadsNode BuildNode() 
    {
        var variableAssignment = new VariableAssignment();
        if (_internalStartedThreadVar.JsonPath != null) 
        {
            variableAssignment.JsonPath = _internalStartedThreadVar.JsonPath;
        }
        variableAssignment.VariableName = _internalStartedThreadVar.Name;
        
        var waitNode = new WaitForThreadsNode
        {
            ThreadList = variableAssignment
        };
        
        return waitNode;
    }
}