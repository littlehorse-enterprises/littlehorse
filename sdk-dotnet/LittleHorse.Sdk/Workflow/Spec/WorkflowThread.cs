using Google.Protobuf;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using static LittleHorse.Sdk.Common.Proto.FailureHandlerDef.Types;
using static LittleHorse.Sdk.Common.Proto.UTActionTrigger.Types;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// This class is used to define the logic of a ThreadSpec in a ThreadFunc. 
/// </summary>
public class WorkflowThread
{
    /// <value>
    /// The parent workflow of this thread.
    /// </value>
    public Workflow Parent { get; init; }
    
    /// <value>
    /// The name of the last node in the thread.
    /// </value>
    public string LastNodeName { get; private set; }

    /// <summary>
    /// This is the reserved Variable Name that can be used as a WfRunVariable in an Interrupt
    /// Handler or Exception Handler thread.
    /// </summary>
    public const string HandlerInputVar = "INPUT";
    
    private readonly ThreadSpec _spec;
    private readonly List<WfRunVariable> _wfRunVariables;
    private EdgeCondition? _lastNodeCondition;
    private readonly Queue<VariableMutation> _variableMutations;
    private ThreadRetentionPolicy? _retentionPolicy;
    
    internal bool IsActive { get; }
    
    internal WorkflowThread(Workflow parent, Action<WorkflowThread> action)
    {
        Parent = parent ?? throw new ArgumentNullException(nameof(parent));
        _spec = new ThreadSpec();
        _wfRunVariables = new List<WfRunVariable>();
        _variableMutations = new Queue<VariableMutation>();

        var entrypointNode = new Node { Entrypoint = new EntrypointNode() };

        var entrypointNodeName = "0-entrypoint-ENTRYPOINT";
        LastNodeName = entrypointNodeName;
        _spec.Nodes.Add(entrypointNodeName, entrypointNode);
        IsActive = true;
        Parent.Threads.Push(this);
        action.Invoke(this);

        var lastNode = FindNode(LastNodeName);
        if (lastNode.NodeCase != Node.NodeOneofCase.Exit) 
        {
            AddNode("exit", Node.NodeOneofCase.Exit, new ExitNode());
        }
        IsActive = false;
        
        _spec.RetentionPolicy = GetRetentionPolicy();
    }

    /// <summary>
    /// This is a compilation method for workflows
    /// </summary>
    /// <returns>A ThreadSpec.</returns>
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
    
    internal Node FindNode(string nodeName)
    {
        if (!_spec.Nodes.TryGetValue(nodeName, out var node))
        {
            throw new ArgumentException("Node not found.");
        }

        return node;
    }
    
    private void CheckIfWorkflowThreadIsActive() 
    {
        if (!IsActive) 
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
            case Node.NodeOneofCase.WaitForCondition:
                node.WaitForCondition = (WaitForConditionNode) subNode;
                break;
            case Node.NodeOneofCase.ThrowEvent:
                node.ThrowEvent = (ThrowEventNode) subNode;
                break;
            case Node.NodeOneofCase.UserTask:
                node.UserTask = (UserTaskNode) subNode;
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
    
    /// <summary>
    /// Defines a Variable in the `ThreadSpec` and returns a handle to it.
    /// 
    /// </summary>
    /// <param name="name">
    /// The name of the variable.
    /// </param>
    /// <param name="typeOrDefaultVal">
    /// It is either the type of the variable, from the `VariableType` enum,
    /// or an object representing the default value of the Variable. If an object (or primitive)
    /// is provided, the Task Worker Library casts the provided value to a VariableValue and sets
    /// that as the default.
    /// </param>
    /// <returns>A handle to the created WfRunVariable.</returns>
    public WfRunVariable AddVariable(string name, object typeOrDefaultVal) 
    {
        CheckIfWorkflowThreadIsActive();
        var wfRunVariable = new WfRunVariable(name, typeOrDefaultVal, this);
        _wfRunVariables.Add(wfRunVariable);
        
        return wfRunVariable;
    }
    
    private ThreadRetentionPolicy? GetRetentionPolicy() 
    {
        if (_retentionPolicy == null) 
            return Parent.GetDefaultThreadRetentionPolicy();

        return _retentionPolicy;
    }
    
    /// <summary>
    /// Overrides the retention policy for all ThreadRun's of this ThreadSpec in the
    /// WfRun.
    /// </summary>
    /// <param name="policy">
    /// It is the Thread Retention Policy.
    /// </param>
    public void WithRetentionPolicy(ThreadRetentionPolicy policy)
    {
        _retentionPolicy = policy;
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
    
    /// <summary>
    /// Adds a TASK node to the ThreadSpec.
    /// 
    /// </summary>
    /// <param name="taskName">
    /// A WfRunVariable containing the name of the TaskDef to execute.
    /// </param>
    /// <param name="args">
    /// The input parameters to pass into the Task Run. If the type of arg is a
    /// `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
    /// library will attempt to cast the provided argument to a LittleHorse VariableValue and
    /// pass that literal value in.
    /// </param>
    /// <returns>A NodeOutput for that TASK node.</returns>
    public TaskNodeOutput Execute(WfRunVariable taskName, params object[] args)
    {
        CheckIfWorkflowThreadIsActive();
        TaskNode taskNode = CreateTaskNode(
            new TaskNode { DynamicTask = AssignVariableHelper(taskName) }, args);
        string nodeName = AddNode(taskName.Name, Node.NodeOneofCase.Task, taskNode);
        
        return new TaskNodeOutput(nodeName, this);
    }
    
    /// <summary>
    /// Adds a TASK node to the ThreadSpec.
    /// 
    /// </summary>
    /// <param name="taskName">
    /// An LHFormatString containing the name of the TaskDef to execute.
    /// </param>
    /// <param name="args">
    /// The input parameters to pass into the Task Run. If the type of arg is a
    /// `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
    /// library will attempt to cast the provided argument to a LittleHorse VariableValue and
    /// pass that literal value in.
    /// </param>
    /// <returns>A NodeOutput for that TASK node.</returns>
    public TaskNodeOutput Execute(LHFormatString taskName, params object[] args)
    {
        CheckIfWorkflowThreadIsActive();
        TaskNode taskNode = CreateTaskNode(
            new TaskNode { DynamicTask = AssignVariableHelper(taskName) }, args);
        string nodeName = AddNode(taskName.Format, Node.NodeOneofCase.Task, taskNode);

        return new TaskNodeOutput(nodeName, this);
    }

    private VariableAssignment AssignVariable(object variable) 
    {
        CheckIfWorkflowThreadIsActive();
        return AssignVariableHelper(variable);
    }

    private TaskNode CreateTaskNode(TaskNode taskNode, params object[] args)
    {
        foreach (var arg in args)
        {
            taskNode.Variables.Add(AssignVariableHelper(arg));
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
    /// <returns>A ExternalEventNodeOutput for this event.</returns>
    public ExternalEventNodeOutput WaitForEvent(string externalEventDefName) 
    {
        CheckIfWorkflowThreadIsActive();
        var waitNode = new ExternalEventNode
        {
            ExternalEventDefId = new ExternalEventDefId { Name = externalEventDefName }
        };

        Parent.AddExternalEventDefName(externalEventDefName);
        var nodeName = AddNode(externalEventDefName, Node.NodeOneofCase.ExternalEvent, waitNode);
        
        return new ExternalEventNodeOutput(nodeName, this);
    }
    
    /// <summary>
    /// Creates a variable of type BOOL in the ThreadSpec.
    /// </summary>
    /// <param name="name">
    /// It is the name of the variable.
    /// </param>
    /// <returns>The value of WfRunVariable </returns>
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
    /// <returns>The value of WfRunVariable </returns>
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
    /// <returns>The value of WfRunVariable </returns>
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
    /// <returns>The value of WfRunVariable </returns>
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
    /// <returns>The value of WfRunVariable </returns>
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
    /// <returns>The value of WfRunVariable </returns>
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
    /// <returns>The value of WfRunVariable </returns>
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
    /// <param name="ifBody">
    /// It is the block of ThreadSpec code to be executed if the provided WorkflowCondition
    /// is satisfied.
    /// </param>
    /// <param name="elseBody">
    /// It is the block of ThreadSpec code to be executed if the provided
    /// WorkflowCondition is NOT satisfied.
    /// </param>
    /// <remarks>
    /// Use <see cref="WorkflowThread.DoIf(WorkflowCondition, System.Action{WorkflowThread})"/>
    /// and <see cref="WorkflowIfStatement.DoElse(System.Action{WorkflowThread})"/> instead.
    /// </remarks>
    public void DoIf(WorkflowCondition condition, Action<WorkflowThread> ifBody, Action<WorkflowThread>? elseBody = null)
    {
        WorkflowIfStatement ifResult = DoIf(condition, ifBody);
        
        if (elseBody != null)
        {
            ifResult.DoElse(elseBody);
        }
    }

    /// <summary>
    /// Conditionally executes one of two workflow code branches; equivalent to an if() statement
    /// in programming.
    /// </summary>
    /// <param name="condition">
    /// It is the WorkflowCondition to be satisfied.
    /// </param>
    /// <param name="body">
    /// It is the block of ThreadSpec code to be executed if the provided WorkflowCondition
    /// is satisfied.
    /// </param>
    public WorkflowIfStatement DoIf(WorkflowCondition condition, Action<WorkflowThread> body)
    {
        CheckIfWorkflowThreadIsActive();
        var firstNodeName = AddNode("nop", Node.NodeOneofCase.Nop, new NopNode());
        _lastNodeCondition = condition.Compile();

        body.Invoke(this);

        var lastNodeName = AddNode("nop", Node.NodeOneofCase.Nop, new NopNode());

        var firstNopeNode = FindNode(firstNodeName);
        firstNopeNode.OutgoingEdges.Add(new Edge
        {
            SinkNodeName = lastNodeName
        });
        
        return new WorkflowIfStatement(this, firstNodeName, lastNodeName);
    }
    
    internal void OrganizeEdgesForElseIfExecution(WorkflowIfStatement ifStatement, 
        Action<WorkflowThread> body, WorkflowCondition? condition = null)
    {
        var firstNopNode = FindNode(ifStatement.FirstNopNodeName);
        var elseEdge = GetLastRemovedEdgeFrom(firstNopNode);
        var lastNodeNameOfParentThread = LastNodeName;
    
        body.Invoke(this);
    
        var lastNodeNameOfBody = LastNodeName;
    
        if (lastNodeNameOfParentThread == lastNodeNameOfBody)
        {
            var edge = GetNewEdge(ifStatement.LastNopNodeName, condition, CollectVariableMutations());
            firstNopNode.OutgoingEdges.Add(edge);
        }         
        else
        {
            var lastNodeOfParentThread = FindNode(lastNodeNameOfParentThread);
            var lastOutgoingEdge = GetLastRemovedEdgeFrom(lastNodeOfParentThread);
            var firstNodeNameOfBody = lastOutgoingEdge.SinkNodeName;
            var edge = GetNewEdge(firstNodeNameOfBody, condition, lastOutgoingEdge.VariableMutations);
            firstNopNode.OutgoingEdges.Add(edge);
            
            var lastNodeOfBody = FindNode(lastNodeNameOfBody);
            var edgeFromLastNodeOfBody = GetNewEdge(ifStatement.LastNopNodeName, null,
                CollectVariableMutations());
            lastNodeOfBody.OutgoingEdges.Add(edgeFromLastNodeOfBody);
        }
        
        if (condition != null)
        {
            firstNopNode.OutgoingEdges.Add(elseEdge);
        }
        LastNodeName = lastNodeNameOfParentThread;
    }
    
    private Edge GetLastRemovedEdgeFrom(Node node)
    {
        if (node.OutgoingEdges == null || node.OutgoingEdges.Count == 0)
        {
            throw new InvalidOperationException("No edges to remove.");
        }

        var index = node.OutgoingEdges.Count - 1;
        var lastEdge = node.OutgoingEdges.ElementAt(index);
        node.OutgoingEdges.Remove(lastEdge);
        
        return lastEdge;
    }

    private Edge GetNewEdge(string sinkNodeName, WorkflowCondition? condition, 
        ICollection<VariableMutation> variableMutations)
    {
        if (condition != null)
        {
            var compiledCondition = condition.Compile();

            return new Edge
            {
                SinkNodeName = sinkNodeName,
                Condition = compiledCondition,
                VariableMutations = { variableMutations }
            };
        }
        
        return new Edge
        {
            SinkNodeName = sinkNodeName,
            VariableMutations = { variableMutations }
        };
    }

    /// <summary>
    /// Conditionally executes some workflow code; equivalent to an while() statement in programming.
    /// </summary>
    /// <param name="condition">
    /// It is the WorkflowCondition to be satisfied.
    /// </param>
    /// <param name="whileThread">
    /// It is the block of ThreadFunc code to be executed while the provided
    /// WorkflowCondition is satisfied.
    /// </param>
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
    /// <param name="comparator">
    /// It is a Comparator defining the comparator, for example,
    /// `ComparatorType.Equals`.
    /// </param>
    /// <param name="rhs">
    /// It is either a literal value (which the Library casts to a Variable Value) or a
    /// `WfRunVariable` representing the RHS of the expression.
    /// </param>
    /// <returns>The value of WorkflowCondition </returns>
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
                Lhs = AssignVariableHelper(expr.Lhs),
                Operation = expr.Operation,
                Rhs = AssignVariableHelper(expr.Rhs),
            };
        }
        else if (value.GetType() == typeof(LHFormatString)) 
        {
            var format = (LHFormatString) value;
            variableAssignment.FormatString = new VariableAssignment.Types.FormatString
            {
                Format = AssignVariableHelper(format.Format),
                Args = { format.Args }
            };
        }
        else
        {
            VariableValue defVal = LHMappingHelper.ObjectToVariableValue(value);
            variableAssignment.LiteralValue = defVal;
        }

        return variableAssignment;
    }
    
    internal void AddTimeoutToExtEvtNode(ExternalEventNodeOutput node, int timeoutSeconds) 
    {
        CheckIfWorkflowThreadIsActive();
        Node newNode = FindNode(node.NodeName);

        var timeoutValue = new VariableAssignment
        {
            LiteralValue = new VariableValue { Int = timeoutSeconds }
        };
        
        newNode.ExternalEvent.TimeoutSeconds = timeoutValue;
    }
    
    internal void AddTimeoutToTaskNode(TaskNodeOutput node, int timeoutSeconds) 
    {
        CheckIfWorkflowThreadIsActive();
        Node newNode = FindNode(node.NodeName);

        newNode.Task.TimeoutSeconds = timeoutSeconds;
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
    /// <returns>A WaitForThreadsNodeOutput that can be used for timeouts or exception handling. </returns>
    public WaitForThreadsNodeOutput WaitForThreads(SpawnedThreads threadsToWaitFor)
    {
        CheckIfWorkflowThreadIsActive();
        WaitForThreadsNode waitNode = threadsToWaitFor.BuildNode();
        string nodeName = AddNode("threads", Node.NodeOneofCase.WaitForThreads, waitNode);
        return new WaitForThreadsNodeOutput(nodeName, this);
    }
    
    /// <summary>
    /// Adds an EXIT node with no Failure defined. This causes the ThreadRun to complete gracefully.
    /// It is equivalent to putting a call to `return;` early in your function.
    /// </summary>
    public void Complete() 
    {
        CheckIfWorkflowThreadIsActive();
        var exitNode = new ExitNode();
        
        AddNode("complete", Node.NodeOneofCase.Exit, exitNode);
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
    
    /// <summary>
    /// Adds a SLEEP node which makes the ThreadRun sleep until a specified timestamp, provided as an
    /// INT WfRunVariable (note that INT in LH is a 64-bit integer).
    /// </summary>
    /// <param name="timestamp">
    /// a WfRunVariable which evaluates to a VariableTypePb.INT specifying the epoch
    /// timestamp (in milliseconds) to wait for.
    /// </param>
    public void SleepUntil(WfRunVariable timestamp)
    {
        CheckIfWorkflowThreadIsActive();
        var sleepNode = new SleepNode { Timestamp = AssignVariable(timestamp) };
        AddNode("sleep", Node.NodeOneofCase.Sleep, sleepNode);
    }
    
    /// <summary>
    /// Adds a WAIT_FOR_CONDITION node which blocks until the provided boolean condition
    /// evaluates to true.
    /// </summary>
    /// <param name="condition">
    /// It is the condition to wait for.
    /// </param>
    /// <returns>A handle to the WaitForConditionNodeOutput, which may only be used for error handling since 
    /// the output of this node is empty.
    /// </returns>
    public WaitForConditionNodeOutput WaitForCondition(WorkflowCondition condition)
    {
        CheckIfWorkflowThreadIsActive();
        WaitForConditionNode waitNode = new WaitForConditionNode
        {
            Condition = condition.Compile()
        };

        string nodeName = AddNode("wait-for-condition", Node.NodeOneofCase.WaitForCondition, waitNode);
        
        return new WaitForConditionNodeOutput(nodeName, this);
    }
    
    /// <summary>
    /// EXPERIMENTAL: Makes the active ThreadSpec throw a WorkflowEvent with a specific WorkflowEventDef
    /// and provided content.
    /// </summary>
    /// <param name="workflowEventDefName">
    /// It is the name of the WorkflowEvent to throw.
    /// </param>
    /// <param name="content">
    /// It is the content of the WorkflowEvent that is thrown.
    /// </param>
    public void ThrowEvent(string workflowEventDefName, object content)
    {
        CheckIfWorkflowThreadIsActive();
        Parent.AddWorkflowEventDefName(workflowEventDefName);
        var node = new ThrowEventNode
        {
            EventDefId = new WorkflowEventDefId
            {
                Name = workflowEventDefName
            },
            Content = AssignVariable(content)
        };
        
        AddNode("throw-" + workflowEventDefName, Node.NodeOneofCase.ThrowEvent, node);
    }
    
    /// <summary>
    /// Adds a Reminder Task to a User Task Node.
    /// </summary>
    /// <param name="userTask">
    /// It is a reference to the UserTaskNode that we schedule the action after.
    /// </param>
    /// <param name="delaySeconds">
    /// It is the delay time after which the Task should be executed.
    /// </param>
    /// <param name="taskDefName">
    /// The name of the TaskDef to execute.
    /// </param>
    /// <param name="args">
    /// The input parameters to pass into the Task Run. If the type of arg is a 
    /// `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
    ///  library will attempt to cast the provided argument to a LittleHorse VariableValue and
    ///  pass that literal value in.
    /// </param>
    public void ScheduleReminderTask(UserTaskOutput userTask, int delaySeconds, string taskDefName, params object[] args)
    {
        ScheduleTaskAfterHelper(userTask, delaySeconds, taskDefName, UTHook.OnArrival, args);
    }
    
    /// <summary>
    /// Adds a Reminder Task to a User Task Node.
    /// </summary>
    /// <param name="userTask">
    /// It is a reference to the UserTaskNode that we schedule the action after.
    /// </param>
    /// <param name="delaySeconds">
    /// It is the delay time after which the Task should be executed.
    /// </param>
    /// <param name="taskDefName">
    /// The name of the TaskDef to execute.
    /// </param>
    /// <param name="args">
    /// The input parameters to pass into the Task Run. If the type of arg is a 
    /// `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
    ///  library will attempt to cast the provided argument to a LittleHorse VariableValue and
    ///  pass that literal value in.
    /// </param>
    public void ScheduleReminderTask(
        UserTaskOutput userTask, WfRunVariable delaySeconds, string taskDefName, params object[] args)
    {
        ScheduleTaskAfterHelper(userTask, delaySeconds, taskDefName, UTHook.OnArrival, args);
    }
    
    private void ScheduleTaskAfterHelper(
        UserTaskOutput userTask, object delaySeconds, string taskDefName, UTHook utHook, params object[] args) 
    {
        CheckIfWorkflowThreadIsActive();
        VariableAssignment delaySecondsParsed = AssignVariableHelper(delaySeconds);
        TaskNode taskNode = CreateTaskNode(
            new TaskNode { TaskDefId = new TaskDefId { Name = taskDefName } }, args);
        Parent.AddTaskDefName(taskDefName);
        UTATask utaTask = new UTATask
        {
            Task = taskNode
        };
        
        if (!LastNodeName.Equals(userTask.NodeName)) {
            throw new InvalidOperationException("Tried to edit a stale User Task node!");
        }

        Node node = FindNode(LastNodeName);
        var newUtAction = new UTActionTrigger
        {
            Task = utaTask,
            Hook = utHook,
            DelaySeconds = delaySecondsParsed
        };
        node.UserTask.Actions.Add(newUtAction);
        // TODO LH-334: return a modified child class of NodeOutput which lets us mutate variables
    }

    /// <summary>
    /// Adds a task reminder once a user is assigned to UserTask.
    /// </summary>
    /// <param name="userTask">
    /// It is a reference to the UserTaskNode that we schedule the action after.
    /// </param>
    /// <param name="delaySeconds">
    /// It is the delay time after which the Task should be executed.
    /// </param>
    /// <param name="taskDefName">
    /// The name of the TaskDef to execute.
    /// </param>
    /// <param name="args">
    /// The input parameters to pass into the Task Run. If the type of arg is a 
    /// ``WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
    ///  library will attempt to cast the provided argument to a LittleHorse VariableValue and
    ///  pass that literal value in.
    /// </param>
    public void ScheduleReminderTaskOnAssignment(
        UserTaskOutput userTask, WfRunVariable delaySeconds, string taskDefName, params object[] args)
    {
        ScheduleTaskAfterHelper(userTask, delaySeconds, taskDefName, UTHook.OnTaskAssigned, args);
    }
    
    /// <summary>
    /// Adds a task reminder once a user is assigned to UserTask.
    /// </summary>
    /// <param name="userTask">
    /// It is a reference to the UserTaskNode that we schedule the action after.
    /// </param>
    /// <param name="delaySeconds">
    /// It is the delay time after which the Task should be executed.
    /// </param>
    /// <param name="taskDefName">
    /// The name of the TaskDef to execute.
    /// </param>
    /// <param name="args">
    /// The input parameters to pass into the Task Run. If the type of arg is a 
    /// ``WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
    ///  library will attempt to cast the provided argument to a LittleHorse VariableValue and
    ///  pass that literal value in.
    /// </param>
    public void ScheduleReminderTaskOnAssignment(
        UserTaskOutput userTask, int delaySeconds, string taskDefName, params object[] args)
    {
        ScheduleTaskAfterHelper(userTask, delaySeconds, taskDefName, UTHook.OnTaskAssigned, args);
    }
    
    /// <summary>
    /// Cancels a User Task Run if it exceeds a specified deadline.
    /// </summary>
    /// <param name="userTask">
    /// It is a reference to the UserTaskNode that will be canceled after the deadline
    /// </param>
    /// <param name="delaySeconds">
    /// It is the delay time after which the User Task Run should be canceled
    /// </param>
    public void CancelUserTaskRunAfter(UserTaskOutput userTask, object delaySeconds)
    {
        ScheduleUserTaskCancellationAfterDeadline(userTask, delaySeconds, UTHook.OnArrival);
    }
    
    /// <summary>
    /// Cancels a User Task Run if it exceeds a specified deadline after it is assigned
    /// </summary>
    /// <param name="userTask">
    /// It is a reference to the UserTaskNode that will be canceled after the deadline
    /// </param>
    /// <param name="delaySeconds">
    /// It is the delay time after which the User Task Run should be canceled
    /// </param>
    public void CancelUserTaskRunAfterAssignment(UserTaskOutput userTask, object delaySeconds)
    {
        ScheduleUserTaskCancellationAfterDeadline(userTask, delaySeconds, UTHook.OnTaskAssigned);
    }
    
    private void ScheduleUserTaskCancellationAfterDeadline(
        UserTaskOutput userTask, object delaySeconds, UTHook hook) 
    {
        CheckIfWorkflowThreadIsActive();
        VariableAssignment delaySecondsParsed = AssignVariableHelper(delaySeconds);
        var utaCancel = new UTACancel();

        if (!LastNodeName.Equals(userTask.NodeName)) 
        {
            throw new InvalidOperationException("Tried to edit a stale User Task node!");
        }
        Node node = FindNode(LastNodeName);
        var newUtAction = new UTActionTrigger
        {
            Cancel = utaCancel,
            Hook = hook,
            DelaySeconds = delaySecondsParsed
        };
        node.UserTask.Actions.Add(newUtAction);
    }
    
    /// <summary>
    /// Adds a User Task Node, and assigns it to a specific user
    /// </summary>
    /// <param name="userTaskDefName">
    /// It is the UserTaskDef to assign.
    /// </param>
    /// <param name="userId">
    /// It is the user id to assign it to. Can be either string or WfRunVariable.
    /// Can be null if userGroup is not null.
    /// </param>
    /// <param name="userGroup">
    /// It is the user group to assign it to. Can be either string or WfRunVariable.
    /// Can be null if userId is not null.
    /// </param>
    /// <returns>A NodeOutput</returns>
    public UserTaskOutput AssignUserTask(string userTaskDefName, object? userId, object? userGroup)
    {
        CheckIfWorkflowThreadIsActive();
        
        ValidateUserIdAndUserGroup(userId, userGroup);
        
        var utNode = new UserTaskNode
        {
            UserTaskDefName = userTaskDefName
        };
        
        if (userId != null) 
        {
            VariableAssignment userIdAssn = AssignVariableHelper(userId);
            utNode.UserId = userIdAssn;
        }

        if (userGroup != null) 
        {
            VariableAssignment userGroupAssn = AssignVariableHelper(userGroup);
            utNode.UserGroup = userGroupAssn;
        }

        // TODO LH-313: Return a special subclass of NodeOutputImpl that allows for adding trigger actions
        string nodeName = AddNode(userTaskDefName, Node.NodeOneofCase.UserTask, utNode);
        return new UserTaskOutput(nodeName, this);
    }
    
    /// <summary>
    /// Schedule Reassignment of a UserTask to a userGroup upon reaching the Deadline. This method is
    /// used to schedule the reassignment of a UserTask to a userGroup when the specified UserTask
    /// user assignment reaches its deadline in seconds.
    /// </summary>
    /// <param name="userTaskOutput">
    /// The NodeOutput that is currently assigned to a UserGroup.
    /// </param>
    /// <param name="deadlineSeconds">
    /// Time in seconds after which the UserTask will be automatically
    /// reassigned to the UserGroup. Can be either string or WfRunVariable.
    /// </param>
    public void ReleaseToGroupOnDeadline(UserTaskOutput userTaskOutput, object deadlineSeconds)
    {
        CheckIfWorkflowThreadIsActive();
        Node currentNode = FindNode(LastNodeName);
        if (!LastNodeName.Equals(userTaskOutput.NodeName)) 
        {
            throw new InvalidOperationException("Tried to edit a stale User Task node!");
        }
        if (currentNode.UserTask.UserId == null) 
        {
            throw new InvalidOperationException("The User Task is not assigned to any user.");
        }
        if (currentNode.UserTask.UserGroup == null) 
        {
            throw new InvalidOperationException("The User Task is assigned to a user without a group.");
        }
        
        VariableAssignment userGroup = currentNode.UserTask.UserGroup;
        ReassignToGroupOnDeadline(userGroup, currentNode, deadlineSeconds);
    }
    
    private void ReassignToGroupOnDeadline(VariableAssignment userGroup, Node currentNode, object deadlineSeconds)
    {
        var reassignPb = new UTAReassign
        {
            UserGroup = userGroup
        };
        var actionTrigger = new UTActionTrigger
        {
            Reassign = reassignPb,
            Hook = UTHook.OnTaskAssigned,
            DelaySeconds = AssignVariableHelper(deadlineSeconds)
        };
        currentNode.UserTask.Actions.Add(actionTrigger);
    }
    
    /// <summary>
    /// Schedules the reassignment of a User Task to a specified userId and/or userGroup after
    /// a specified expiration.
    /// </summary>
    /// <param name="userTask">
    /// It is the userTask to reschedule.
    /// </param>
    /// <param name="userId">
    /// It is the userId to which the task should be assigned. Must be either WfRunVariable
    /// or string. Can be null if userGroup not null.
    /// </param>
    /// <param name="userGroup">
    /// It is the userGroup to which the task should be reassigned. Must be either
    /// WfRunVariable or string. Can be null if userId not null.
    /// </param>
    /// <param name="deadlineSeconds">
    /// It is the expiration time after which the UserTask should be reassigned.
    /// Can be either WfRunVariable or string.
    /// </param>
    public void ReassignUserTask(UserTaskOutput userTask, object? userId, object? userGroup, object deadlineSeconds)
    {
        CheckIfWorkflowThreadIsActive();
        Node currentNode = FindNode(LastNodeName);
        
        ValidateUserIdAndUserGroup(userId, userGroup);
        
        if (!LastNodeName.Equals(userTask.NodeName)) 
        {
            throw new InvalidOperationException("Tried to edit a stale User Task node!");
        }

        var reassignment = new UTAReassign
        {
            UserId = AssignVariableHelper(userId!)
        };
            
        if (userGroup != null) 
        {
            reassignment.UserGroup = AssignVariableHelper(userGroup);
        }
        if (userId != null) 
        {
            reassignment.UserId = AssignVariableHelper(userId);
        }

        var actionTrigger = new UTActionTrigger
        {
            Reassign = reassignment,
            Hook = UTHook.OnTaskAssigned,
            DelaySeconds = AssignVariableHelper(deadlineSeconds)
        };
        currentNode.UserTask.Actions.Add(actionTrigger);
    }
    
    /// <summary>
    /// Creates a formatted string using WfRunVariables as arguments.
    ///
    /// Example:
    /// Format("Hello there, {0}, today is {1}", name, dayOfWeek);
    /// 
    /// </summary>
    /// <param name="format">
    /// It is the format string.
    /// </param>
    /// <param name="args">
    /// They are the format args.
    /// or string. Can be null if userGroup not null.
    /// </param>
    public LHFormatString Format(string format, params WfRunVariable[] args)
    {
        return new LHFormatString(this, format, args);
    }

    private void ValidateUserIdAndUserGroup(object? userId, object? userGroup)
    {
        if (userId == null && userGroup == null)
        {
            throw new ArgumentException("userId or userGroup is required.");
        }

        if (userId is string userIdValue && string.IsNullOrWhiteSpace(userIdValue))
        {
            throw new ArgumentException("userId can't be empty.");
        }
        
        if (userGroup is string userGroupValue && string.IsNullOrWhiteSpace(userGroupValue))
        {
            throw new ArgumentException("userGroup can't be empty.");
        }
    }
}