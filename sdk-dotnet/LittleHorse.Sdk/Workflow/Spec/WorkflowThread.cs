using Google.Protobuf;
using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WorkflowThread
{
    private String _name;
    private Workflow _parentWorkflow;
    private ThreadSpec _spec;
    public string LastNodeName;
    private bool _isActive;
    private List<WfRunVariable> _wfRunVariables;
    
    public WorkflowThread(String name, Workflow parent, Action<WorkflowThread> action)
    {
        _name = name;
        _parentWorkflow = parent;
        _spec = new ThreadSpec();
        _wfRunVariables = new List<WfRunVariable>();

        var entrypointNode = new Node { Entrypoint = new EntrypointNode() };

        String entrypointNodeName = "0-entrypoint-ENTRYPOINT";
        LastNodeName = entrypointNodeName;
        _spec.Nodes.Add(entrypointNodeName, entrypointNode);
        _isActive = true;
        action.Invoke(this);

        var lastNode = FindNode(LastNodeName);
        if (lastNode.NodeCase != Node.NodeOneofCase.Exit) {
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

    private Node FindNode(string nodeName)
    {
        if (_spec.Nodes.Last().Key  != nodeName)
        {
            throw new ArgumentException("No node found");
        }

        return _spec.Nodes[nodeName];
    }
    
    private void CheckIfWorkflowThreadIsActive() {
        if (!_isActive) {
            throw new InvalidOperationException("Using a inactive thread");
        }
    }
    
    private string AddNode(string name, Node.NodeOneofCase type, IMessage subNode) {
        CheckIfWorkflowThreadIsActive();
        String nextNodeName = GetNodeName(name, type);
        if (LastNodeName == null) {
            throw new InvalidOperationException("Not possible to have null last node here");
        }

        var feederNode = FindNode(LastNodeName);
        var edge = new Edge {SinkNodeName = nextNodeName};
        
        if (feederNode.NodeCase != Node.NodeOneofCase.Exit) {
            feederNode.OutgoingEdges.Add(edge);
            _spec.Nodes[LastNodeName] = feederNode;
        }

        Node node = new Node();
        switch (type) {
            case Node.NodeOneofCase.Task:
                node.Task = (TaskNode) subNode;
                break;
            case Node.NodeOneofCase.Entrypoint:
                node.Task = (TaskNode) subNode;
                break;
            case Node.NodeOneofCase.Exit:
                node.Exit = (ExitNode) subNode;
                break;
            case Node.NodeOneofCase.None:
                // not possible
                throw new InvalidOperationException("Not possible");
        }

        _spec.Nodes.Add(nextNodeName, node);
        LastNodeName = nextNodeName;

        return nextNodeName;
    }
    
    private String GetNodeName(String name, Node.NodeOneofCase type) {
        return $"{_spec.Nodes.Count}-{name}-{type}";
    }
    
    public WfRunVariable AddVariable(String name, Object typeOrDefaultVal) {
        CheckIfWorkflowThreadIsActive();
        var wfRunVariable = new WfRunVariable(name, typeOrDefaultVal, this);
        _wfRunVariables.Add(wfRunVariable);
        
        return wfRunVariable;
    }
}