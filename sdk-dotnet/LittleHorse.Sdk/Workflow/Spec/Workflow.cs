using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Sdk.Workflow.Spec;

using static LittleHorse.Sdk.Common.Proto.LittleHorse;

public class Workflow
{
    private readonly ILogger<Workflow>? _logger;
    private string _name;
    private Action<WorkflowThread> _entryPoint;
    private PutWfSpecRequest? _compiledWorkflow;
    private PutWfSpecRequest _spec;
    private Queue<Tuple<string, Action<WorkflowThread>>> _threadActions;
    private readonly string _parentWfSpecName;
    private HashSet<string> _requiredTaskDefNames;

    public Workflow(string name, Action<WorkflowThread> entryPoint)
    {
        _logger = LHLoggerFactoryProvider.GetLogger<Workflow>();
        _name = name;
        _parentWfSpecName = string.Empty;
        _entryPoint = entryPoint;
        _compiledWorkflow = null!;
        _spec = new PutWfSpecRequest { Name = name };
        _threadActions = new Queue<Tuple<string, Action<WorkflowThread>>>();
        _requiredTaskDefNames = new HashSet<string>();
    }

    public PutWfSpecRequest Compile()
    {
        return _compiledWorkflow ?? CompileWorkflowDetails();
    }

    public void RegisterWfSpec(LittleHorseClient lhClient)
    {
        _logger!.LogInformation(LHMappingHelper.ProtoToJson(lhClient.PutWfSpec(Compile())));
    }

    private String AddSubThread(String subThreadName, Action<WorkflowThread> subThreadAction) 
    {
        foreach (var threadPair in _threadActions)
        {
            if (threadPair.Item1 == subThreadName)
            {
                throw new Exception($"Thread {subThreadName} already exists");
            }
        }
        
        _threadActions.Enqueue(new Tuple<string, Action<WorkflowThread>>(subThreadName, subThreadAction));
        return subThreadName;
    }
    
    private PutWfSpecRequest CompileWorkflowDetails( ) 
    {
        String entrypointThreadName = AddSubThread("entrypoint", _entryPoint);
        _spec.EntrypointThreadName = entrypointThreadName;

        while (_threadActions.Count != 0) 
        {
            Tuple<string, Action<WorkflowThread>> nextThreadAction = _threadActions.Dequeue();
            string actionName = nextThreadAction.Item1;
            Action<WorkflowThread> threadAction = nextThreadAction.Item2;
            WorkflowThread wfThread = new WorkflowThread(_name, this, threadAction);
            _spec.ThreadSpecs.Add(actionName, wfThread.Compile());
        }
        
        if (!string.IsNullOrEmpty(_parentWfSpecName))
        {
            _spec.ParentWfSpec = new WfSpec.Types.ParentWfSpecReference { WfSpecName = _parentWfSpecName };
        }

        return _spec;
    }
    
    internal void AddTaskDefName(String taskDefName) 
    {
        _requiredTaskDefNames.Add(taskDefName);
    }
}