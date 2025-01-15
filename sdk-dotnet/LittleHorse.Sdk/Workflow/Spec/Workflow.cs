using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

using static LittleHorse.Sdk.Common.Proto.LittleHorse;

public class Workflow
{
    private string _name;
    private Action<WorkflowThread> _entryPoint;
    private PutWfSpecRequest _compiledWorkflow;
    private PutWfSpecRequest _spec;
    private Queue<Tuple<string, Action<WorkflowThread>>> _threadActions;


    public Workflow(string name, Action<WorkflowThread> entryPoint)
    {
        _name = name;
        _compiledWorkflow = null!;
        _entryPoint = entryPoint;
        _spec = new PutWfSpecRequest { Name = name };
        _threadActions = new Queue<Tuple<string, Action<WorkflowThread>>>();
    }

    private PutWfSpecRequest CompileWorkflow()
    {
        return _compiledWorkflow ??= CompileWorkflowDetails();
    }

    public void RegisterWfSpec(LittleHorseClient lhClient)
    {
        lhClient.PutWfSpec(CompileWorkflow());
    }
    
    public String AddSubThread(String subThreadName, Action<WorkflowThread> subThreadAction) {
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
    
    private PutWfSpecRequest CompileWorkflowDetails( ) {
        String entrypointThreadName = AddSubThread("entrypoint", _entryPoint);
        _spec.EntrypointThreadName = entrypointThreadName;

        while (_threadActions.Count != 0) {
            Tuple<string, Action<WorkflowThread>> nextThreadAction = _threadActions.Dequeue();
            string actionName = nextThreadAction.Item1;
            WorkflowThread wfThread = new WorkflowThread(_name, this);
            _spec.ThreadSpecs.Add(actionName, wfThread.Compile());
        }

        return _spec;
    }
}