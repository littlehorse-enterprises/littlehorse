using Google.Protobuf;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using static LittleHorse.Sdk.Common.Proto.FailureHandlerDef.Types;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WorkflowThread
{
    public Workflow Parent { get; private set; }
    private readonly ThreadSpec _spec;
    public string LastNodeName;
    private readonly bool _isActive;
    private readonly List<WfRunVariable> _wfRunVariables;
    private EdgeCondition? _lastNodeCondition;
    private readonly Queue<VariableMutation> _variableMutations;
    
    /// <summary>
    /// This is the reserved Variable Name that can be used as a WfRunVariable in an Interrupt
    /// Handler or Exception Handler thread.
    /// </summary>
    public const string HandlerInputVar = "INPUT";
    
    public WorkflowThread(Workflow parent, Action<WorkflowThread> action)
    {
        Parent = parent;
        _spec = new ThreadSpec();
        _wfRunVariables = new List<WfRunVariable>();
        _variableMutations = new Queue<VariableMutation>();

        var entrypointNode = new Node { Entrypoint = new EntrypointNode() };

        var entrypointNodeName = "0-entrypoint-ENTRYPOINT";
        LastNodeName = entrypointNodeName;
        _spec.Nodes.Add(entrypointNodeName, entrypointNode);
        _isActive = true;
        action.Invoke(this);

        var lastNode = FindLastNode();
        if (lastNode.NodeCase != Node.NodeOneofCase.Exit) 
        {
            AddNode("exit", Node.NodeOneofCase.Exit, new ExitNode());
        }
        _isActive = false;

        // TODO: Take into account the retention policy
    }

    public ThreadSpec Compile()
    {
        if (_spec.VariableDefs.Count > 0)
        {
            _spec.VariableDefs.Clear();
        }

        foreach (var wfRunVariable in _wfRunVariables)
        {
            _spec.VariableDefs!.Add(wfRunVariable.Compile());
        }
        
        return _spec;
    }

    private Node FindLastNode()
    {
        if (_spec.Nodes.Last().Key  != LastNodeName)
        {
            throw new ArgumentException("No node found.");
        }

        return _spec.Nodes[LastNodeName];
    }
    
    private Node FindNode(string nodeName)
    {
        if (!_spec.Nodes.TryGetValue(nodeName, out var node))
        {
            throw new ArgumentException("Node not found.");
        }

        return node;
    }
    
    private void CheckIfWorkflowThreadIsActive() 
    {
        if (!_isActive) 
        {
            throw new InvalidOperationException("Using an inactive thread");
        }
    }
    
    private string AddNode(string name, Node.NodeOneofCase type, IMessage subNode) 
    {
        CheckIfWorkflowThreadIsActive();
        string nextNodeName = GetNodeName(name, type);
        if (LastNodeName == null) 
        {
            throw new InvalidOperationException("Not possible to have null last node here");
        }

        var feederNode = FindNode(LastNodeName);
        var edge = new Edge { SinkNodeName = nextNodeName };
        
        edge.VariableMutations.AddRange(CollectVariableMutations());
        
        if (_lastNodeCondition != null) 
        {
            edge.Condition = _lastNodeCondition;
            _lastNodeCondition = null;
        }
        
        if (feederNode.NodeCase != Node.NodeOneofCase.Exit) 
        {
            feederNode.OutgoingEdges.Add(edge);
            _spec.Nodes[LastNodeName] = feederNode;
        }

        Node node = new Node();
        switch (type) 
        {
            case Node.NodeOneofCase.Task:
                node.Task = (TaskNode) subNode;
                break;
            case Node.NodeOneofCase.Entrypoint:
                node.Entrypoint = (EntrypointNode) subNode;
                break;
            case Node.NodeOneofCase.Nop:
                node.Nop = (NopNode) subNode;
                break;
            case Node.NodeOneofCase.ExternalEvent:
                node.ExternalEvent = (ExternalEventNode) subNode;
                break;
            case Node.NodeOneofCase.Exit:
                node.Exit = (ExitNode) subNode;
                break;
            case Node.NodeOneofCase.StartThread:
                node.StartThread = (StartThreadNode) subNode;
                break;
            case Node.NodeOneofCase.StartMultipleThreads:
                node.StartMultipleThreads = (StartMultipleThreadsNode) subNode;
                break;
            case Node.NodeOneofCase.WaitForThreads:
                node.WaitForThreads = (WaitForThreadsNode) subNode;
                break;
            case Node.NodeOneofCase.Sleep:
                node.Sleep = (SleepNode) subNode;
                break;
            case Node.NodeOneofCase.None:
                throw new InvalidOperationException("Not possible");
        }

        _spec.Nodes.Add(nextNodeName, node);
        LastNodeName = nextNodeName;

        return nextNodeName;
    }
    
    private string GetNodeName(string name, Node.NodeOneofCase type) 
    {
        return $"{_spec.Nodes.Count}-{name}-{LHConstants.NodeTypes[type]}";
    }
    
    public WfRunVariable AddVariable(string name, Object typeOrDefaultVal) 
    {
        CheckIfWorkflowThreadIsActive();
        var wfRunVariable = new WfRunVariable(name, typeOrDefaultVal, this);
        _wfRunVariables.Add(wfRunVariable);
        
        return wfRunVariable;
    }
    
    /// <summary>
    /// Adds a TASK node to the ThreadSpec.
    /// 
    /// </summary>
    /// <param name="taskName">
    /// It is the name of the TaskDef to execute.
    /// </param>
    /// <param name="args">
    /// It is the input parameters to pass into the Task Run. If the type of arg is a
    /// `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
    /// library will attempt to cast the provided argument to a LittleHorse VariableValue and
    /// pass that literal value in.
    /// </param>
    /// <returns>A NodeOutput for that TASK node.</returns>
    public TaskNodeOutput Execute(string taskName, params object[] args) 
    {
        CheckIfWorkflowThreadIsActive();
        Parent.AddTaskDefName(taskName);
        var taskNode = CreateTaskNode(
            new TaskNode { TaskDefId = new TaskDefId { Name = taskName } }, args);
        string nodeName = AddNode(taskName, Node.NodeOneofCase.Task, taskNode);
        
        return new TaskNodeOutput(nodeName, this);
    }

    private VariableAssignment AssignVariable(Object variable) 
    {
        CheckIfWorkflowThreadIsActive();
        return AssignVariableHelper(variable);
    }

    private TaskNode CreateTaskNode(TaskNode taskNode, params object[] args)
    {
        foreach (var arg in args)
        {
            taskNode.Variables.Add(AssignVariable(arg));
        }

        if (Parent.GetDefaultTaskTimeout() != 0)
        {
            taskNode.TimeoutSeconds = Parent.GetDefaultTaskTimeout();
        }

        taskNode.Retries = Parent.GetDefaultSimpleRetries();

        if (Parent.GetDefaultExponentialBackoffRetryPolicy() != null)
        {
            taskNode.ExponentialBackoff = 
                Parent.GetDefaultExponentialBackoffRetryPolicy();
        }

        return taskNode;
    }
    
    /// <summary>
    /// Adds an EXTERNAL_EVENT node which blocks until an 'ExternalEvent' of the specified type
    /// arrives.
    /// </summary>
    /// <param name="externalEventDefName">
    /// It is the type of ExternalEvent to wait for.
    /// </param>
    /// <returns>A NodeOutput for this event.</returns>
    public NodeOutput WaitForEvent(string externalEventDefName) 
    {
        CheckIfWorkflowThreadIsActive();
        var waitNode = new ExternalEventNode
        {
            ExternalEventDefId = new ExternalEventDefId { Name = externalEventDefName }
        };

        Parent.AddExternalEventDefName(externalEventDefName);
        var nodeName = AddNode(externalEventDefName, Node.NodeOneofCase.ExternalEvent, waitNode);
        
        return new NodeOutput(nodeName, this);
    }
    
    /// <summary>
    /// Creates a variable of type BOOL in the ThreadSpec.
    /// </summary>
    /// <param name="name">
    /// It is the name of the variable.
    /// </param>
    /// <returns>The value of <paramref name="WfRunVariable" /> </returns>
    public WfRunVariable DeclareBool(string name) 
    {
        return AddVariable(name, VariableType.Bool);
    }
    
    /// <summary>
    /// Creates a variable of type INT in the ThreadSpec.
    /// </summary>
    /// <param name="name">
    /// It is the name of the variable.
    /// </param>
    /// <returns>The value of <paramref name="WfRunVariable" /> </returns>
    public WfRunVariable DeclareInt(string name) 
    {
        return AddVariable(name, VariableType.Int);
    }
    
    /// <summary>
    /// Creates a variable of type STR in the ThreadSpec.
    /// </summary>
    /// <param name="name">
    /// It is the name of the variable.
    /// </param>
    /// <returns>The value of <paramref name="WfRunVariable" /> </returns>
    public WfRunVariable DeclareStr(string name) 
    {
        return AddVariable(name, VariableType.Str);
    }
    
    /// <summary>
    /// Creates a variable of type DOUBLE in the ThreadSpec.
    /// </summary>
    /// <param name="name">
    /// It is the name of the variable.
    /// </param>
    /// <returns>The value of <paramref name="WfRunVariable" /> </returns>
    public WfRunVariable DeclareDouble(string name) 
    {
        return AddVariable(name, VariableType.Double);
    }
    
    /// <summary>
    /// Creates a variable of type BYTES in the ThreadSpec.
    /// </summary>
    /// <param name="name">
    /// It is the name of the variable.
    /// </param>
    /// <returns>The value of <paramref name="WfRunVariable" /> </returns>
    public WfRunVariable DeclareBytes(string name) 
    {
        return AddVariable(name, VariableType.Bytes);
    }
    
    /// <summary>
    /// Creates a variable of type JSON_ARR in the ThreadSpec.
    /// </summary>
    /// <param name="name">
    /// It is the name of the variable.
    /// </param>
    /// <returns>The value of <paramref name="WfRunVariable" /> </returns>
    public WfRunVariable DeclareJsonArr(string name) 
    {
        return AddVariable(name, VariableType.JsonArr);
    }
    
    /// <summary>
    /// Creates a variable of type JSON_OBJ in the ThreadSpec.
    /// </summary>
    /// <param name="name">
    /// It is the name of the variable.
    /// </param>
    /// <returns>The value of <paramref name="WfRunVariable" /> </returns>
    public WfRunVariable DeclareJsonObj(string name) 
    {
        return AddVariable(name, VariableType.JsonObj);
    }
    
    private List<VariableMutation> CollectVariableMutations() 
    {
        var variablesFromIfBlock = new List<VariableMutation>();
        while (_variableMutations.Count > 0) 
        {
            variablesFromIfBlock.Add(_variableMutations.Dequeue());
        }
        
        return variablesFromIfBlock;
    }
    
    /// <summary>
    /// Conditionally executes one of two workflow code branches; equivalent to an if/else statement
    /// in programming.
    /// </summary>
    /// <param name="condition">
    /// It is the WorkflowCondition to be satisfied.
    /// </param>
    /// /// <param name="ifBody">
    /// It is the block of ThreadSpec code to be executed if the provided WorkflowCondition
    /// is satisfied.
    /// </param>
    /// /// <param name="elseBody">
    /// It is the block of ThreadSpec code to be executed if the provided
    /// WorkflowCondition is NOT satisfied.
    /// </param>
    public void DoIf(WorkflowCondition condition, Action<WorkflowThread> ifBody, Action<WorkflowThread>? elseBody = null) 
    {
        CheckIfWorkflowThreadIsActive();
        
        AddNode("nop", Node.NodeOneofCase.Nop, new NopNode());
        var treeRootNodeName = LastNodeName;
        _lastNodeCondition = condition.Compile();

        ifBody.Invoke(this);
        
        var lastConditionFromIfBlock = _lastNodeCondition;
        var lastNodeFromIfBlockName = LastNodeName;
        var variablesFromIfBlock = CollectVariableMutations();

        if (elseBody != null)
        {
            LastNodeName = treeRootNodeName;
            _lastNodeCondition = condition.GetOpposite();
            
            elseBody.Invoke(this);
            
            AddNode("nop", Node.NodeOneofCase.Nop, new NopNode());
            var lastNodeFromIfBlock = FindNode(lastNodeFromIfBlockName);
            var ifBlockEdge = new Edge { SinkNodeName = LastNodeName };
            ifBlockEdge.VariableMutations.AddRange(variablesFromIfBlock);
            if (lastNodeFromIfBlockName == treeRootNodeName)
            {
                ifBlockEdge.Condition = lastConditionFromIfBlock;
            }
            lastNodeFromIfBlock.OutgoingEdges.Add(ifBlockEdge);
        }
        else
        {
            AddNode("nop", Node.NodeOneofCase.Nop, new NopNode());

            var treeRoot = FindNode(treeRootNodeName);
            treeRoot.OutgoingEdges.Add(new Edge
            {
                SinkNodeName = LastNodeName,
                Condition = condition.GetOpposite()
            });
        }
    }

    public void DoWhile(WorkflowCondition condition, Action<WorkflowThread> whileThread)
    {
        CheckIfWorkflowThreadIsActive();

        AddNode("nop", Node.NodeOneofCase.Nop, new NopNode());
        var treeRootNodeName = LastNodeName;
        _lastNodeCondition = condition.Compile();
        
        whileThread.Invoke(this);
        
        AddNode("nop", Node.NodeOneofCase.Nop, new NopNode());
        
        var treeLastNodeName = LastNodeName;
        
        var treeRoot = FindNode(treeRootNodeName);
        treeRoot.OutgoingEdges.Add(new Edge
        {
            SinkNodeName = treeLastNodeName,
            Condition = condition.GetOpposite()
        });

        var treeLast = FindNode(treeLastNodeName);
        treeLast.OutgoingEdges.Add(new Edge
        {
            SinkNodeName = treeRootNodeName,
            Condition = condition.Compile()
        });
    }
    
    /// <summary>
    /// Returns a WorkflowCondition used in `WorkflowThread::doIf()`
    /// </summary>
    /// <param name="lhs">
    /// It is either a literal value (which the Library casts to a Variable Value) or a
    /// `WfRunVariable` representing the LHS of the expression.
    /// </param>
    /// <param name="rhs">
    /// It is either a literal value (which the Library casts to a Variable Value) or a
    /// `WfRunVariable` representing the RHS of the expression.
    /// </param>
    /// <returns>The value of <paramref name="WorkflowCondition" /> </returns>
    public WorkflowCondition Condition(object lhs, Comparator comparator, object rhs)
    {
        return new WorkflowCondition(AssignVariableHelper(lhs), 
            comparator, AssignVariableHelper(rhs));
    }
    
    /// <summary>
    /// Adds a VariableMutation to the last Node
    /// </summary>
    /// <param name="lhs">It is a handle to the WfRunVariable to mutate.</param>
    /// <param name="type">It is the mutation type to use, for example, `VariableMutationType.ASSIGN`.</param>
    /// <param name="rhs">
    /// It is either a literal value (which the Library casts to a Variable Value), a
    /// `WfRunVariable` which determines the right hand side of the expression, or a `NodeOutput`
    /// (which allows you to use the output of a Node Run to mutate variables).
    /// </param>
    public void Mutate(WfRunVariable lhs, VariableMutationType type, object rhs)
    {
        CheckIfWorkflowThreadIsActive();
        var mutation = new VariableMutation
        {
            LhsName = lhs.Name,
            Operation = type,
            RhsAssignment = AssignVariableHelper(rhs)
        };
        if (lhs.JsonPath != null)
        {
            mutation.LhsJsonPath = lhs.JsonPath;
        }

        _variableMutations.Enqueue(mutation);
    }
    
    internal VariableAssignment AssignVariableHelper(object? value)
    {
        var variableAssignment = new VariableAssignment();

        if (value == null)
        {
            variableAssignment.LiteralValue = new VariableValue();
        }
        else if (value.GetType() == typeof(WfRunVariable))
        {
            var wrVariable = (WfRunVariable) value;
            
            if (wrVariable.JsonPath != null) 
            {
                variableAssignment.JsonPath = wrVariable.JsonPath;
            }
            variableAssignment.VariableName = wrVariable.Name;
        } 
        else if (value is NodeOutput nodeReference)
        {
            // We can use the new `VariableAssignment` feature: NodeOutputReference
            var nodeOutputReference = new VariableAssignment.Types.NodeOutputReference
            {
                NodeName = nodeReference.NodeName
            };
            variableAssignment.NodeOutput = nodeOutputReference;

            if (nodeReference.JsonPath != null)
            {
                variableAssignment.JsonPath = nodeReference.JsonPath;
            }
        }
        else if (value is LHExpression expr) 
        {
            variableAssignment.Expression = new VariableAssignment.Types.Expression
            {
                Lhs = AssignVariable(expr.Lhs),
                Operation = expr.Operation,
                Rhs = AssignVariable(expr.Rhs),
            };
        }
        // TODO: Add else if condition to format strings
        else
        {
            VariableValue defVal = LHMappingHelper.ObjectToVariableValue(value);
            variableAssignment.LiteralValue = defVal;
        }

        return variableAssignment;
    }
    
    internal void AddTimeoutToExtEvt(NodeOutput node, int timeoutSeconds) 
    {
        CheckIfWorkflowThreadIsActive();
        Node newNode = FindNode(node.NodeName);

        var timeoutValue = new VariableAssignment
        {
            LiteralValue = new VariableValue { Int = timeoutSeconds }
        };

        if (newNode.NodeCase == Node.NodeOneofCase.Task)
        {
            newNode.Task.TimeoutSeconds = timeoutSeconds;
        } 
        else if (newNode.NodeCase == Node.NodeOneofCase.ExternalEvent) 
        {
            newNode.ExternalEvent.TimeoutSeconds = timeoutValue;
        } 
        else 
        {
            throw new Exception("Timeouts are only supported on ExternalEvent and Task nodes.");
        }
    }
    
    internal void OverrideTaskExponentialBackoffPolicy(TaskNodeOutput node, ExponentialBackoffRetryPolicy policy)
    {
        var newNode = CheckTaskNode(node);

        newNode.Task.ExponentialBackoff = policy;
    }

    internal void OverrideTaskRetries(TaskNodeOutput node, int retries) 
    {
        var newNode = CheckTaskNode(node);

        newNode.Task.Retries = retries;
    }
    
    private Node CheckTaskNode(TaskNodeOutput node)
    {
        CheckIfWorkflowThreadIsActive();
        Node newNode = FindNode(node.NodeName);
        
        if (newNode.NodeCase != Node.NodeOneofCase.Task) 
        {
            throw new InvalidOperationException("Impossible to not have task node here");
        }

        return newNode;
    }
    
    /// <summary>
    /// Attaches an Error Handler to the specified NodeOutput, allowing it to manage specific types of errors
    /// as defined by the 'error' parameter. If 'error' is set to null, the handler will catch all errors.
    /// </summary>
    /// <param name="node">
    /// The NodeOutput instance to which the Error Handler will be attached.
    /// </param>
    /// <param name="error">
    /// The type of error that the handler will manage.
    /// </param>
    /// <param name="handler">
    /// A ThreadFunction defining a ThreadSpec that specifies how to handle the error.
    /// </param>
    public void HandleError(NodeOutput node, LHErrorType error, Action<WorkflowThread> handler)
    {
        CheckIfWorkflowThreadIsActive();
        var errorFormatted = LHConstants.ErrorTypes[error.ToString()];
        var handlerDef = BuildFailureHandlerDef(node, 
            errorFormatted, 
            handler);
        handlerDef.SpecificFailure = errorFormatted;
        AddFailureHandlerDef(handlerDef, node);
    }
    
    /// <summary>
    /// Attaches an Error Handler to the specified NodeOutput, allowing it to manage any types of errors.
    /// 
    /// </summary>
    /// <param name="node">
    /// The NodeOutput instance to which the Error Handler will be attached.
    /// </param>
    /// <param name="handler">
    /// A ThreadFunction defining a ThreadSpec that specifies how to handle the error.
    /// </param>
    public void HandleError(NodeOutput node, Action<WorkflowThread> handler)
    {
        CheckIfWorkflowThreadIsActive();
        var handlerDef = BuildFailureHandlerDef(node, 
            LHConstants.FailureTypes[LHFailureType.FailureTypeError], 
            handler);
        handlerDef.AnyFailureOfType = LHFailureType.FailureTypeError;
        AddFailureHandlerDef(handlerDef, node);
    }
    
    /// <summary>
    /// Adds an EXIT node with a Failure defined. This causes a ThreadRun to fail, and the resulting
    /// Failure has the specified value, name, and human-readable message.
    /// </summary>
    /// <param name="output">
    /// It is a literal value (cast to VariableValue by the Library) or a WfRunVariable.
    ///     The assigned value is the payload of the resulting Failure, which can be accessed by any
    ///     Failure Handler ThreadRuns.
    /// </param>
    /// <param name="failureName">
    /// It is the name of the failure to throw.
    /// </param>
    /// <param name="message">
    /// It is a human-readable message.
    /// </param>
    public void Fail(object? output, string failureName, string? message)
    {
        CheckIfWorkflowThreadIsActive();
        var failureDef = new FailureDef();
        if (output != null) failureDef.Content = AssignVariable(output);
        if (message != null) failureDef.Message = message;
        failureDef.FailureName = failureName;

        ExitNode exitNode = new ExitNode { FailureDef = failureDef };

        AddNode(failureName, Node.NodeOneofCase.Exit, exitNode);
    }
    
    /// <summary>
    /// Adds an EXIT node with a Failure defined. This causes a ThreadRun to fail, and the resulting
    /// Failure has the specified name and human-readable message.
    /// </summary>
    /// <param name="failureName">
    /// It is the name of the failure to throw.
    /// </param>
    /// <param name="message">
    /// It is a human-readable message.
    /// </param>
    public void Fail(string failureName, string message) 
    {
        Fail(null, failureName, message);
    }
    
    private FailureHandlerDef BuildFailureHandlerDef(NodeOutput node, string error, Action<WorkflowThread> handler)
    {
        string suffix = !string.IsNullOrEmpty(error) ? $"-{error}" : string.Empty;
        string threadName = $"exn-handler-{node.NodeName}{suffix}";

        threadName = Parent.AddSubThread(threadName, handler);
        
        return new FailureHandlerDef { HandlerSpecName = threadName };
    }
    
    private void AddFailureHandlerDef(FailureHandlerDef handlerDef, NodeOutput node)
    {
        // Add the failure handler to the most recent node
        Node lastNode = FindNode(node.NodeName);

        lastNode.FailureHandlers.Add(handlerDef);
    }
    
    /// <summary>
    /// Attaches an Exception Handler to the specified NodeOutput, enabling it to handle specific
    /// types of exceptions as defined by the 'exceptionName' parameter. If 'exceptionName' is null,
    /// the handler will catch all exceptions.
    /// </summary>
    /// <param name="node">
    /// The NodeOutput instance to which the Exception Handler will be attached.
    /// </param>
    /// <param name="exceptionName">
    /// The name of the specific exception to handle. If set to null, the handler will catch all exceptions.
    /// </param>
    /// <param name="handler">
    /// A ThreadFunction defining a ThreadSpec that specifies how to handle the exception.
    /// </param>
    public void HandleException(NodeOutput node, string exceptionName, Action<WorkflowThread> handler)
    {
        CheckIfWorkflowThreadIsActive();
        var handlerDef = BuildFailureHandlerDef(node, exceptionName, handler);
        handlerDef.SpecificFailure = exceptionName;
        AddFailureHandlerDef(handlerDef, node);
    }
    
    /// <summary>
    /// Attaches an Exception Handler to the specified NodeOutput, enabling it to handle any
    /// types of exceptions.
    /// </summary>
    /// <param name="node">
    /// The NodeOutput instance to which the Exception Handler will be attached.
    /// </param>
    /// <param name="handler">
    /// A ThreadFunction defining a ThreadSpec that specifies how to handle the exception.
    /// </param>
    public void HandleException(NodeOutput node, Action<WorkflowThread> handler)
    {
        CheckIfWorkflowThreadIsActive();
        var handlerDef = BuildFailureHandlerDef(node, null!, handler);
        handlerDef.AnyFailureOfType = LHFailureType.FailureTypeException;
        AddFailureHandlerDef(handlerDef, node);
    }

    /// <summary>
    /// Attaches a Failure Handler to the specified NodeOutput, allowing it manages any type of errors or exceptions.
    /// types of exceptions.
    /// </summary>
    /// <param name="node">
    /// The NodeOutput instance to which the Error Handler will be attached.
    /// </param>
    /// <param name="handler">
    /// A ThreadFunction defining a ThreadSpec that specifies how to handle the error.
    /// </param>
    public void HandleAnyFailure(NodeOutput node, Action<WorkflowThread> handler)
    {
        CheckIfWorkflowThreadIsActive();
        var handlerDef = BuildFailureHandlerDef(node, LHConstants.AnyFailure, handler);
        AddFailureHandlerDef(handlerDef, node);
    }
    
    /// <summary>
    /// Adds a WAIT_FOR_THREAD node which waits for a Child ThreadRun to complete.
    /// </summary>
    /// <param name="threadsToWaitFor">
    /// Set of SpawnedThread objects returned one or more calls to spawnThread.
    /// </param>
    /// <returns>A NodeOutput that can be used for timeouts or exception handling. </returns>
    public WaitForThreadsNodeOutput WaitForThreads(SpawnedThreads threadsToWaitFor)
    {
        CheckIfWorkflowThreadIsActive();
        WaitForThreadsNode waitNode = threadsToWaitFor.BuildNode();
        string nodeName = AddNode("threads", Node.NodeOneofCase.WaitForThreads, waitNode);
        return new WaitForThreadsNodeOutput(nodeName, this);
    }
    
    /// <summary>
    /// Adds a SPAWN_THREAD node to the ThreadSpec, which spawns a Child ThreadRun whose ThreadSpec
    /// is determined by the provided ThreadFunc.
    /// </summary>
    /// <param name="threadFunc">
    /// It is a ThreadFunc (can be a lambda function) that defines the logic for the child ThreadRun to execute.
    /// </param>
    /// <param name="threadName">
    /// It is the name of the child thread spec.
    /// </param>
    /// <param name="inputVars">
    /// It is a Dictionary of all the input variables to set for the child ThreadRun. If
    ///     you don't need to set any input variables, leave this null.
    /// </param>
    /// <returns>A handle to the resulting SpawnedThread, which can be used in ThreadBuilder::WaitForThread()</returns>
    public SpawnedThread SpawnThread(string threadName, Action<WorkflowThread> threadFunc, 
        Dictionary<string, object>? inputVars=null)
    {
        CheckIfWorkflowThreadIsActive();
        inputVars ??= new Dictionary<string, object>();
        var subThreadName = Parent.AddSubThread(threadName, threadFunc);

        var variableAssignments = new Dictionary<string, VariableAssignment>();
        foreach (var inputVar in inputVars)
        {
            variableAssignments.Add(inputVar.Key, AssignVariable(inputVar.Value));
        }

        var startThread = new StartThreadNode
        {
            ThreadSpecName = subThreadName,
            Variables = { variableAssignments }
        };

        string nodeName = AddNode(subThreadName, Node.NodeOneofCase.StartThread, startThread);
        WfRunVariable internalStartedThreadVar = DeclareInt(nodeName);

        // The output of a StartThreadNode is just an integer containing the name
        // of the thread.
        internalStartedThreadVar.Assign(new NodeOutput(nodeName, this));

        return new SpawnedThread(this, subThreadName, internalStartedThreadVar);
    }
    
    internal void AddFailureHandlerOnWaitForThreadsNode(WaitForThreadsNodeOutput node, FailureHandlerDef handler) 
    {
        CheckIfWorkflowThreadIsActive();
        var currentNode = FindNode(node.NodeName);

        if (currentNode.NodeCase != Node.NodeOneofCase.WaitForThreads) 
        {
            throw new InvalidOperationException("This should only be a WAIT_FOR_THREADS node");
        }

        var subBuilder = currentNode.WaitForThreads;
        subBuilder.PerThreadFailureHandlers.Add(handler);
        currentNode.WaitForThreads = subBuilder;
    }
    
    /// <summary>
    /// Given a WfRunVariable of type JSON_ARR, this function iterates over each object in that list
    /// and creates a Child ThreadRun for each item. The list item is provided as an input variable
    /// to the Child ThreadRun with the name `INPUT`.
    /// </summary>
    /// <param name="arrVar">
    /// It is a WfRunVariable of type JSON_ARR that we iterate over.
    /// </param>
    /// <param name="threadName">
    /// It is the name to assign to the created ThreadSpec.
    /// </param>
    /// <param name="threadFunc">
    /// It is the function that defines the ThreadSpec.
    /// </param>
    /// <param name="inputVars">
    /// It is a dictionary of input variables to pass to each child ThreadRun in addition
    ///     to the list item.
    /// </param>
    /// <returns>a SpawnedThreads handle which we can use to wait for all child threads.</returns>
    public SpawnedThreads SpawnThreadForEach(
        WfRunVariable arrVar, string threadName, Action<WorkflowThread> threadFunc, 
        Dictionary<string, object>? inputVars = null)
    {
        CheckIfWorkflowThreadIsActive();
        string finalThreadName = Parent.AddSubThread(threadName, threadFunc);
        var startMultiplesThreadNode = new StartMultipleThreadsNode
        {
            ThreadSpecName = finalThreadName,
            Iterable = AssignVariable(arrVar)
        };

        if (inputVars != null)
            foreach (var inputVar in inputVars)
            {
                startMultiplesThreadNode.Variables.Add(inputVar.Key, AssignVariable(inputVar.Value));
            }

        string nodeName = AddNode(threadName, Node.NodeOneofCase.StartMultipleThreads, startMultiplesThreadNode);
        WfRunVariable internalStartedThreadVar = DeclareJsonArr(nodeName);
        internalStartedThreadVar.Assign(new NodeOutput(nodeName, this));

        return new SpawnedThreadsIterator(internalStartedThreadVar);
    }
    
    /// <summary>
    /// Registers an Interrupt Handler, such that when an ExternalEvent arrives with the specified
    /// type, this ThreadRun is interrupted.
    /// </summary>
    /// <param name="interruptName">
    /// The name of the ExternalEventDef to listen for.
    /// </param>
    /// <param name="handler">
    /// A Thread Function defining a ThreadSpec to use to handle the Interrupt.
    /// </param>
    /// <returns>A NodeOutput that can be used for timeouts or exception handling. </returns>
    public void RegisterInterruptHandler(string interruptName, Action<WorkflowThread> handler)
    {
        CheckIfWorkflowThreadIsActive();
        string threadName = "interrupt-" + interruptName;
        Parent.AddSubThread(threadName, handler);
        Parent.AddExternalEventDefName(interruptName);

        _spec.InterruptDefs.Add(
            new InterruptDef
            {
                ExternalEventDefId = new ExternalEventDefId { Name = interruptName },
                HandlerSpecName = threadName
            }
        );
    }
    
    /// <summary>
    /// Adds a SLEEP node which makes the ThreadRun sleep for a specified number of seconds.
    /// 
    /// </summary>
    /// <param name="seconds">
    /// It is either an integer representing the number of seconds to sleep for, or it is 
    /// a WfRunVariable which evaluates to a VariableTypePb.INT specifying the number of seconds 
    /// to sleep for.
    /// </param>
    public void SleepSeconds(object seconds)
    {
        CheckIfWorkflowThreadIsActive();
        var sleepNode = new SleepNode { RawSeconds = AssignVariable(seconds) };
        AddNode("sleep", Node.NodeOneofCase.Sleep, sleepNode);
    }
}