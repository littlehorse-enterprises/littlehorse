using Google.Protobuf;
using Grpc.Core;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Sdk.Workflow.Spec;

using static LittleHorse.Sdk.Common.Proto.LittleHorse;

/// <summary>
/// The <c>Workflow</c> class represents a workflow definition in the LittleHorse system.
/// </summary>
public class Workflow
{
    private readonly ILogger<Workflow>? _logger;
    private string _name;
    private readonly Action<WorkflowThread> _entryPoint;
    private PutWfSpecRequest? _compiledWorkflow;
    private readonly PutWfSpecRequest _spec;
    private readonly Queue<Tuple<string, Action<WorkflowThread>>> _threadActions;
    private readonly string _parentWfSpecName;
    private readonly HashSet<string> _requiredTaskDefNames;
    private readonly HashSet<string> _requiredEedNames;
    private readonly HashSet<string> _requiredWorkflowEventDefNames;
    private int _defaultTaskTimeout;
    private int _defaultSimpleRetries;
    private ExponentialBackoffRetryPolicy _defaultExponentialBackoff = null!;
    private ThreadRetentionPolicy? _defaultThreadRetentionPolicy;
    private WorkflowRetentionPolicy? _wfRetentionPolicy;
    internal readonly Stack<WorkflowThread> Threads;
    private readonly List<ThrowEventNodeOutput> _workflowEventsToRegister = new();

    /// <summary>
    /// Initializes a new instance of the <see cref="Workflow"/> class.
    /// </summary>
    /// <param name="name">The name of the workflow.</param>
    /// <param name="entryPoint">This is the main entrypoint for the workflow logic.</param>
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
        _requiredWorkflowEventDefNames = new HashSet<string>();
        Threads = new Stack<WorkflowThread>();
        // Force workflow construction here so all event registrations are done before Compile
        // var wfThread = new WorkflowThread(this, entryPoint);
        // _spec.ThreadSpecs.Add("entrypoint", wfThread.Compile());
    }
    
    /// <summary>
    /// Compiles this Workflow into a `WfSpec`.
    /// </summary>
    /// <returns>
    /// A `PutWfSpecRequest` that can be used for the gRPC putWfSpec() call.
    /// </returns>
    public PutWfSpecRequest Compile()
    {
        return _compiledWorkflow ??= CompileWorkflowDetails();
    }

    /// <summary>
    /// Deploys the WfSpec object to the LH Server. Registering the WfSpec via
    /// Workflow::RegisterWfSpec() is the same as client.putWfSpec(workflow.compileWorkflow()).
    /// </summary>
    /// <param name="client">
    /// It is a LHClient.
    /// </param>
    public void RegisterWfSpec(LittleHorseClient client)
    {
        var request = Compile();
        foreach (var node in _workflowEventsToRegister)
        {
            client.PutWorkflowEventDef(node.ToPutWorkflowEventDefRequest());
            _logger!.LogInformation($"Registered WorkflowEventDef: {node.ToPutWorkflowEventDefRequest().Name}");
        }
        _logger!.LogInformation($"Created wfSpec:\n{LHMappingHelper.ProtoToJson(client.PutWfSpec(request))}");

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
        
        if (_wfRetentionPolicy != null) 
        {
            _spec.RetentionPolicy = _wfRetentionPolicy;
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
    /// Returns the names of all `TaskDef`s used by this workflow.
    /// </summary>
    /// <returns>
    /// A HashSet of strings containing the names of all `TaskDef`s used by this workflow.
    /// </returns>
    public HashSet<string> GetRequiredTaskDefNames()
    {
        return _requiredTaskDefNames;
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
    /// Tells the Workflow to configure (by default) the specified ExponentialBackoffRetryPolicy as
    /// the retry policy.
    ///
    /// Can be overriden by setting the retry policy on the WorkflowThread or TaskNodeOutput level.
    /// </summary>
    /// <param name="defaultPolicy">
    /// It is the Exponential Backoff Retry Policy to configure by default for all Task Nodes.
    /// </param>
    public void SetDefaultTaskExponentialBackoffPolicy(ExponentialBackoffRetryPolicy defaultPolicy) 
    {
        _defaultExponentialBackoff = defaultPolicy;
    }

    /// <summary>
    /// Gets the workflow name passed at <code>new Workflow(string, Action)</code>
    /// </summary>
    /// <returns>
    /// The Workflow name
    /// </returns>
    public string GetName() 
    {
        return _name;
    }
    
    /// <summary>
    /// Returns the names of all `ExternalEventDef`s used by this workflow. Includes
    /// ExternalEventDefs used for Interrupts or for EXTERNAL_EVENT nodes.
    /// 
    /// </summary>
    /// <returns>
    /// A HashSet of strings containing the names of all `ExternalEventDef`s used by this workflow.
    /// </returns>
    public HashSet<string> GetRequiredExternalEventDefNames()
    {
        _compiledWorkflow ??= CompileWorkflowDetails();
        return _requiredEedNames;
    }
    
    /// <summary>
    /// Returns the names of all `WorkflowEventDef`s used by this workflow.
    /// 
    /// </summary>
    /// <returns>
    /// A HashSet of strings containing the names of all `WorkflowEventDef`s thrown by this workflow.
    /// </returns>
    public HashSet<string> GetRequiredWorkflowEventDefNames()
    {
        return _requiredWorkflowEventDefNames;
    }
    
    internal void AddWorkflowEventDefName(string name) 
    {
        _requiredWorkflowEventDefNames.Add(name);
    }
    
    internal void AddWorkflowEventDefToRegister(ThrowEventNodeOutput node)
    {
        _workflowEventsToRegister.Add(node);
    }
    
    internal ThreadRetentionPolicy? GetDefaultThreadRetentionPolicy() 
    {
        return _defaultThreadRetentionPolicy;
    }
    
    /// <summary>
    /// Sets the retention policy for all ThreadRun's belong to this WfSpec.
    /// 
    /// Note that each Thread can override the configured Retention Policy by
    /// using WorkflowThread#withRetentionPolicy.
    /// </summary>
    /// <param name="policy">
    /// It is the Workflow Retention Policy.
    /// </param>
    /// <returns>
    /// This Workflow.
    /// </returns>
    public Workflow WithDefaultThreadRetentionPolicy(ThreadRetentionPolicy? policy)
    {
        _defaultThreadRetentionPolicy = policy;
        
        return this;
    }
    
    /// <summary>
    /// Defines the type of update to perform when saving the WfSpec:
    /// AllowedUpdateType.ALL (Default): Creates a new WfSpec with a different version (either major or revision).
    /// AllowedUpdateType.MINOR_REVISION_ONLY: Creates a new WfSpec with a different revision if the change is a major version it fails.
    /// AllowedUpdateType.NONE: Fail with the ALREADY_EXISTS response code.
    /// </summary>
    /// <param name="allowedUpdateType">
    /// It is the type of allowed update.
    /// </param>
    /// <returns>
    /// This Workflow.
    /// </returns>
    public Workflow WithUpdateType(AllowedUpdateType allowedUpdateType) 
    {
        _spec.AllowedUpdates = allowedUpdateType;
        
        return this;
    }
    
    /// <summary>
    /// Sets the retention policy for all WfRun's created by this WfSpec.
    /// 
    /// using WorkflowThread#withRetentionPolicy.
    /// </summary>
    /// <param name="policy">
    /// It is the Workflow Retention Policy.
    /// </param>
    /// <returns>
    /// This Workflow.
    /// </returns>
    public Workflow WithRetentionPolicy(WorkflowRetentionPolicy policy) 
    {
        _wfRetentionPolicy = policy;
        
        return this;
    }
    
    /// <summary>
    /// Checks if the WfSpec exists for a given version
    /// </summary>
    /// <param name="client">
    /// It is an LHClient.
    /// </param>
    /// <param name="majorVersion">
    /// This is an optional parameter which represents the workflow Major Version.
    /// </param>
    /// <returns>
    /// True if the workflow spec is registered with/without Major Version or false otherwise
    /// </returns>
    public bool DoesWfSpecExist(LittleHorseClient client, int? majorVersion = null) 
    {
        try
        {
            // TODO: LH-282, support revision versioning here.
            var wfSpecId = new WfSpecId { Name = _name };
            if (majorVersion != null)
            {
                wfSpecId.MajorVersion = majorVersion.Value;
            }
            
            client.GetWfSpec(wfSpecId);
            
            return true;
        }
        catch (RpcException ex) 
        {
            if (ex.StatusCode == StatusCode.NotFound)
            {
                return false;
            }

            throw;
        }
    }
    
    /// <summary>
    /// Returns the associated PutWfSpecRequest in JSON form.
    /// </summary>
    /// <returns>
    /// The associated PutWfSpecRequest in JSON form.
    /// </returns>
    public string? CompileWfToJson() 
    {
        try 
        {
            PutWfSpecRequest wfSpec = Compile();
            return LHMappingHelper.ProtoToJson(wfSpec);
        } 
        catch (InvalidProtocolBufferException exn) 
        {
            throw new Exception($"Cannot compile wfSpec: {exn.Message}");
        }
    }
    
    /// <summary>
    /// Writes out the PutWfSpecRequest in JSON form in a directory.
    /// </summary>
    /// <param name="directory">
    /// It is the location to save the resources.
    /// </param>
    public void CompileAndSaveToDisk(string directory) 
    {
        PutWfSpecRequest wf = Compile();
        string wfFileName = wf.Name + LHConstants.SuffixCompiledWfFileName;
        try
        {
            _logger!.LogInformation($"Saving WfSpec to {wfFileName}");
            SaveProtoToFile(directory, wfFileName, wf);
        }
        catch (Exception e)
        {
            throw new Exception($"Something occurred trying to save file {wfFileName} to disk", e);
        }
    }

    private void SaveProtoToFile(string directory, string fileName, IMessage content) 
    {
        var parentPath = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "../../.."));
        var directoryPath = Path.Combine(parentPath, directory);
        Directory.CreateDirectory(directoryPath);
        string filePath = Path.Combine(directoryPath, fileName);
        string? json = LHMappingHelper.ProtoToJson(content);
        
        File.WriteAllText(filePath, json);
    }
}