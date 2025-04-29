using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WorkflowIfStatement
{
    private readonly WorkflowThread _parent;
    private readonly string _firstNopNodeName;
    private string _lastNopNodeName;
    internal WorkflowIfStatement(WorkflowThread parent, string firstNopNodeName, string lastNopNodeName)
    {
        _parent = parent;
        _firstNopNodeName = firstNopNodeName;
        _lastNopNodeName = lastNopNodeName;
    }

    private Edge GetLastRemovedEdge(ICollection<Edge> outgoingEdges)
    {
        if (outgoingEdges == null || outgoingEdges.Count == 0)
        {
            throw new InvalidOperationException("No edges to remove.");
        }

        var index = outgoingEdges.Count - 1;
        var lastEdge = outgoingEdges.ElementAt(index);
        outgoingEdges.Remove(lastEdge);
        
        return lastEdge;
    }

    public WorkflowIfStatement DoElseIf(WorkflowCondition condition, Action<WorkflowThread> body)
    {
        var firstNopNode = _parent.FindNode(_firstNopNodeName);
        var elseEdge = GetLastRemovedEdge(firstNopNode.OutgoingEdges);
        var compiledCondition = condition.Compile();
        
        var lastNodeNameOfParentThread = _parent.LastNodeName;
        
        body.Invoke(_parent);
        
        var lastNodeNameOfBody = _parent.LastNodeName;
        
        if (lastNodeNameOfParentThread == lastNodeNameOfBody)
        {
            firstNopNode.OutgoingEdges.Add(new Edge
            {
                SinkNodeName = _lastNopNodeName,
                Condition = new EdgeCondition
                {
                    Left = compiledCondition.Left,
                    Comparator = compiledCondition.Comparator,
                    Right = compiledCondition.Right
                },
                VariableMutations = { _parent.CollectVariableMutations() }
            });
        }
        else
        {
            var lastOutgoingEdgesFromParentThread = _parent.FindNode(lastNodeNameOfParentThread).OutgoingEdges;
            var lastOutgoingEdge = GetLastRemovedEdge(lastOutgoingEdgesFromParentThread);
            var firstNodeNameOfBody = lastOutgoingEdge.SinkNodeName;                       
            firstNopNode.OutgoingEdges.Add(new Edge
            {
                SinkNodeName = firstNodeNameOfBody,
                Condition = new EdgeCondition
                {
                    Left = compiledCondition.Left,
                    Comparator = compiledCondition.Comparator,
                    Right = compiledCondition.Right
                } ,
                VariableMutations = { lastOutgoingEdge.VariableMutations }
            });

            _parent.Spec.Nodes.Remove(_lastNopNodeName);
            _lastNopNodeName = _parent.AddNode(_lastNopNodeName, Node.NodeOneofCase.Nop, new NopNode(), 
                keepNodeName: true);
        }

        firstNopNode.OutgoingEdges.Add(elseEdge);
        
        return this;
    }

    private WorkflowIfStatement DoElse(Action<WorkflowThread> body)
    {
       return this;
    }
}