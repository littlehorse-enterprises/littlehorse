using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

using static LittleHorse.Sdk.Common.Proto.LittleHorse;

public class Workflow
{
    private string _name;
    private Action<WorkflowThread> _entryPoint;
    private PutWfSpecRequest _spec;
    private Queue<Tuple<string, Action<WorkflowThread>>> _threadActions;
    private WorkflowRetentionPolicy _wfRetentionPolicy;

    public Workflow(string name, Action<WorkflowThread> entryPoint)
    {
        _name = name;
        _entryPoint = entryPoint;
        _spec = new PutWfSpecRequest { Name = name };
        _threadActions = new Queue<Tuple<string, Action<WorkflowThread>>>();
    }
    
    public PutWfSpecRequest CompileWorkflow() {
        if (_spec == null) {
            CompileWorkflowDetails();
        }
        return _spec;
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

        if (_wfRetentionPolicy != null) {
            _spec.RetentionPolicy = _wfRetentionPolicy;
        }

        return _spec;
    }
    
    public Workflow WithRetentionPolicy(WorkflowRetentionPolicy policy) {
        _wfRetentionPolicy = policy;
        return this;
    }
}