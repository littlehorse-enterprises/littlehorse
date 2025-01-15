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
    
    public WorkflowThread(String name, Workflow parent)
    {
        _name = name;
        _parentWorkflow = parent;
        _spec = new ThreadSpec();

        var entrypointNode = new Node { Entrypoint = new EntrypointNode() };

        String entrypointNodeName = "0-entrypoint-ENTRYPOINT";
        LastNodeName = entrypointNodeName;
        _spec.Nodes.Add(entrypointNodeName, entrypointNode);
        _isActive = true;

        var exitNode = GetLastNode(LastNodeName);
        if (exitNode.NodeCase == Node.NodeOneofCase.Exit) {
            addNode("exit", Node.NodeOneofCase.Exit, new ExitNode());
        }
        _isActive = false;

        // TODO: Take into account the retention policy
    }

    public ThreadSpec Compile()
    {
        return _spec;
    }

    private Node GetLastNode(string lastNodeName)
    {
        if (_spec.Nodes.Last().Key  != lastNodeName)
        {
            throw new ArgumentException("No node found");
        }

        return _spec.Nodes[lastNodeName];
    }
    
    private void CheckIfWorkflowThreadIsActive() {
        if (!_isActive) {
            throw new InvalidOperationException("Using a inactive thread");
        }
    }
    
    private String addNode(String name, Node.NodeOneofCase type, IMessage subNode) {
        CheckIfWorkflowThreadIsActive();
        String nextNodeName = GetNodeName(name, type);
        if (LastNodeName == null) {
            throw new InvalidOperationException("Not possible to have null last node here");
        }

        var feederNode = GetLastNode(LastNodeName);
        var edge = new Edge {SinkNodeName = nextNodeName};
        
        if (feederNode.NodeCase != Node.NodeOneofCase.Exit) {
            feederNode.OutgoingEdges.Add(edge);
            _spec.Nodes.Add(LastNodeName, feederNode);
        }

        Node node = new Node();
        switch (type) {
            case Node.NodeOneofCase.Task:
                node.Task = (TaskNode) subNode;
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
}