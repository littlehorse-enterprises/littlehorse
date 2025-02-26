using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class FixedSpawnedThreads: SpawnedThreads
{
    private readonly ICollection<SpawnedThread> _spawnedThreads;

    public FixedSpawnedThreads(params SpawnedThread[] spawnedThreads) 
    {
        _spawnedThreads = spawnedThreads;
    }
    
    public WaitForThreadsNode BuildNode()
    {
        var waitNode = new WaitForThreadsNode();
        var threads = new List<WaitForThreadsNode.Types.ThreadToWaitFor>();
        foreach (var spawnedThread in _spawnedThreads)
        {
            WfRunVariable threadNumberVariable = spawnedThread.GetThreadNumberVariable();
            if (threadNumberVariable.Type != VariableType.Int) 
            {
                throw new ArgumentException("Only int variables are supported.");
            }
            var variableAssignment = new VariableAssignment();
            if (threadNumberVariable.JsonPath != null) 
            {
                variableAssignment.JsonPath = threadNumberVariable.JsonPath;
            }
            variableAssignment.VariableName = threadNumberVariable.Name;
            var threadToWaitFor = new WaitForThreadsNode.Types.ThreadToWaitFor
            {
                ThreadRunNumber = variableAssignment
            };
            
            threads.Add(threadToWaitFor);
        }
        
        waitNode.Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor { Threads = { threads } };
        return waitNode;
    }
}