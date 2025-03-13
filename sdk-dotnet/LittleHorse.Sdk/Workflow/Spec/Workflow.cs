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
    private readonly HashSet<string> _requiredTaskDefNames;
    private readonly HashSet<string> _requiredEedNames;
    private int _defaultTaskTimeout;
    private int _defaultSimpleRetries;
    internal ExponentialBackoffRetryPolicy _defaultExponentialBackoff = null!;

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
        _requiredEedNames = new HashSet<string>();
    }

    public PutWfSpecRequest Compile()
    {
        return _compiledWorkflow ?? CompileWorkflowDetails();
    }
    
    /// <summary>
    /// Deploys the WfSpec object to the LH Server. Registering the WfSpec via
    /// Workflow::registerWfSpec() is the same as client.putWfSpec(workflow.compileWorkflow()).
    /// </summary>
    /// <param name="client">
    /// It is a LHClient.
    /// </param>
    public void RegisterWfSpec(LittleHorseClient client)
    {
        _logger!.LogInformation(LHMappingHelper.ProtoToJson(client.PutWfSpec(Compile())));
    }

    internal string AddSubThread(string subThreadName, Action<WorkflowThread> subThreadAction) 
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
        string entrypointThreadName = AddSubThread("entrypoint", _entryPoint);
        _spec.EntrypointThreadName = entrypointThreadName;

        while (_threadActions.Count != 0) 
        {
            Tuple<string, Action<WorkflowThread>> nextThreadAction = _threadActions.Dequeue();
            string actionName = nextThreadAction.Item1;
            Action<WorkflowThread> threadAction = nextThreadAction.Item2;
            var wfThread = new WorkflowThread(this, threadAction);
            _spec.ThreadSpecs.Add(actionName, wfThread.Compile());
        }
        
        if (!string.IsNullOrEmpty(_parentWfSpecName))
        {
            _spec.ParentWfSpec = new WfSpec.Types.ParentWfSpecReference { WfSpecName = _parentWfSpecName };
        }

        return _spec;
    }
    
    internal void AddTaskDefName(string taskDefName) 
    {
        _requiredTaskDefNames.Add(taskDefName);
    }
    
    internal void AddExternalEventDefName(string eedName) 
    {
        _requiredEedNames.Add(eedName);
    }
    
    /// <summary>
    /// Returns the default task timeout, or null if it is not set.
    /// </summary>
    /// <returns>
    /// the default task timeout for this Workflow.
    /// </returns>
    public int GetDefaultTaskTimeout() 
    {
        return _defaultTaskTimeout;
    }

    /// <summary>
    /// Sets the default timeout for all TaskRun's in this workflow.
    /// </summary>
    /// <param name="timeoutSeconds">
    /// It is the value for the timeout to set.
    /// </param>
    public void SetDefaultTaskTimeout(int timeoutSeconds) 
    {
        _defaultTaskTimeout = timeoutSeconds;
    }
    
    internal int GetDefaultSimpleRetries() 
    {
        return _defaultSimpleRetries;
    }
    
    /// <summary>
    /// Tells the Workflow to configure (by default) a Simple Retry Policy for every Task Node. Passing
    /// a value of '1' means that there will be one retry upon failure. Retries are scheduled immediately
    /// without delay.
    ///
    /// Can be overriden by setting the retry policy on the WorkflowThread or TaskNodeOutput level.
    /// </summary>
    /// <param name="defaultSimpleRetries">
    /// It is the number of retries to attempt.
    /// </param>
    public void SetDefaultTaskRetries(int defaultSimpleRetries) 
    {
        if (defaultSimpleRetries < 0) 
        {
            throw new ArgumentException("Cannot have negative retries!");
        }
        _defaultSimpleRetries = defaultSimpleRetries;
    }
    
    internal ExponentialBackoffRetryPolicy? GetDefaultExponentialBackoffRetryPolicy() 
    {
        return _defaultExponentialBackoff!;
    }
    
    /// <summary>
    /// Returns the names of all `ExternalEventDef`s used by this workflow. Includes
    /// ExternalEventDefs used for Interrupts or for EXTERNAL_EVENT nodes.
    /// 
    /// </summary>
    /// <returns>
    /// A Set of Strings containing the names of all `ExternalEventDef`s used by this workflow.
    /// </returns>
    public HashSet<string> GetRequiredExternalEventDefNames()
    {
        _compiledWorkflow ??= CompileWorkflowDetails();
        return _requiredEedNames;
    }
}