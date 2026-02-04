using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a collection of spawned threads that are fixed at the time of creation.
/// </summary>
public class SpawnedThreadsIterator: SpawnedThreads
{
    private readonly WfRunVariable _internalStartedThreadVar;

    /// <summary>
    /// Initializes a new instance of the <see cref="SpawnedThreadsIterator"/> class with the specified spawned threads.
    /// </summary>
    /// <param name="internalStartedThreadVar">This is a wfRunVariable of VariableType.JsonArr type</param>
    /// <exception cref="ArgumentException">This exception is raised when the _internalStartedThreadVar parameter
    /// does not have a type of VariableType.JsonArr.</exception>
    public SpawnedThreadsIterator(WfRunVariable internalStartedThreadVar) 
    {
       _internalStartedThreadVar = internalStartedThreadVar;
        if (_internalStartedThreadVar.Type != VariableType.JsonArr) 
        {
            throw new ArgumentException("Only support for json arrays.");
        }
    }
    
    /// <summary>
    /// Builds a <see cref="WaitForThreadsNode"/> that represents the spawned threads.
    /// </summary>
    /// <returns>WaitForThreadsNode</returns>
    public WaitForThreadsNode BuildNode(WaitForThreadsStrategy strategy) 
    {
        var variableAssignment = new VariableAssignment();
        if (_internalStartedThreadVar.JsonPath != null) 
        {
            variableAssignment.JsonPath = _internalStartedThreadVar.JsonPath;
        }
        variableAssignment.VariableName = _internalStartedThreadVar.Name;
        
        var waitNode = new WaitForThreadsNode
        {
            ThreadList = variableAssignment,
            Strategy = strategy
        };
        
        return waitNode;
    }
}