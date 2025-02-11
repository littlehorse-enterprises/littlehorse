using Google.Protobuf;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WorkflowThread
{
    private Workflow _parent;
    private ThreadSpec _spec;
    public string LastNodeName;
    private bool _isActive;
    private List<WfRunVariable> _wfRunVariables;
    private EdgeCondition? _lastNodeCondition;
    private readonly Queue<VariableMutation> _variableMutations;
    
    public WorkflowThread(Workflow parent, Action<WorkflowThread> action)
    {
        _parent = parent;
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
        String nextNodeName = GetNodeName(name, type);
        if (LastNodeName == null) 
        {
            throw new InvalidOperationException("Not possible to have null last node here");
        }

        var feederNode = FindNode(LastNodeName);
        var edge = new Edge { SinkNodeName = nextNodeName };
        
        edge.VariableMutations.AddRange(CollectVariableMutations());
        
        if (_lastNodeCondition != null) {
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
            case Node.NodeOneofCase.Exit:
                node.Exit = (ExitNode) subNode;
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
        return $"{_spec.Nodes.Count}-{name}-{type.ToString().ToUpper()}";
    }
    
    public WfRunVariable AddVariable(string name, Object typeOrDefaultVal) 
    {
        CheckIfWorkflowThreadIsActive();
        var wfRunVariable = new WfRunVariable(name, typeOrDefaultVal, this);
        _wfRunVariables.Add(wfRunVariable);
        
        return wfRunVariable;
    }
    
    public NodeOutput Execute(string taskName, params object[] args) 
    {
        CheckIfWorkflowThreadIsActive();
        _parent.AddTaskDefName(taskName);
        var taskNode = CreateTaskNode(
            new TaskNode { TaskDefId = new TaskDefId { Name = taskName } }, args);
        string nodeName = AddNode(taskName, Node.NodeOneofCase.Task, taskNode);
        
        return new NodeOutput(nodeName, this);
    }
    
    public VariableAssignment AssignVariable(Object variable) 
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

        if (_parent.GetDefaultTaskTimeout() != 0)
        {
            taskNode.TimeoutSeconds = _parent.GetDefaultTaskTimeout();
        }

        taskNode.Retries = _parent.GetDefaultSimpleRetries();

        if (_parent.GetDefaultExponentialBackoffRetryPolicy() != null)
        {
            taskNode.ExponentialBackoff = 
                _parent.GetDefaultExponentialBackoffRetryPolicy();
        }

        return taskNode;
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
}