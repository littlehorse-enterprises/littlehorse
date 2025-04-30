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
        OrganizeEdgesAfterBodyExecution(body, condition);
        
        return this;
    }

    public WorkflowIfStatement DoElse(Action<WorkflowThread> body)
    {
        OrganizeEdgesAfterBodyExecution(body);
        
       return this;
    }

    private void OrganizeEdgesAfterBodyExecution(Action<WorkflowThread> body, WorkflowCondition? condition = null)
    {
        var firstNopNode = _parent.FindNode(_firstNopNodeName);
        var elseEdge = GetLastRemovedEdge(firstNopNode.OutgoingEdges);
        var lastNodeNameOfParentThread = _parent.LastNodeName;
    
        body.Invoke(_parent);
    
        var lastNodeNameOfBody = _parent.LastNodeName;
    
        if (lastNodeNameOfParentThread == lastNodeNameOfBody)
        {
            var edge = GetNewEdge(_lastNopNodeName, condition, _parent.CollectVariableMutations());
            firstNopNode.OutgoingEdges.Add(edge);
        }
        else
        {
            var lastNodeOfParentThread = _parent.FindNode(lastNodeNameOfParentThread);
            var lastOutgoingEdgesFromParentThread = lastNodeOfParentThread.OutgoingEdges;
            var lastOutgoingEdge = GetLastRemovedEdge(lastOutgoingEdgesFromParentThread);
            var firstNodeNameOfBody = lastOutgoingEdge.SinkNodeName;
            var edge = GetNewEdge(firstNodeNameOfBody, condition, lastOutgoingEdge.VariableMutations);
            firstNopNode.OutgoingEdges.Add(edge);

            _parent.Spec.Nodes.Remove(_lastNopNodeName);
            _lastNopNodeName = _parent.AddNode(_lastNopNodeName, Node.NodeOneofCase.Nop, new NopNode(), 
                keepNodeName: true);
        }
        
        if (condition != null)
        {
            firstNopNode.OutgoingEdges.Add(elseEdge);
        }
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
}