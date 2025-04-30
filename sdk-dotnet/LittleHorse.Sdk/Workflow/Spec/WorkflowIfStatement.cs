using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a conditional statement in a workflow.
/// </summary>
public class WorkflowIfStatement
{
    private readonly WorkflowThread _parent;
    private readonly string _firstNopNodeName;
    private string _lastNopNodeName;
    private bool _wasElseExecuted;
    
    internal WorkflowIfStatement(WorkflowThread parent, string firstNopNodeName, string lastNopNodeName)
    {
        _parent = parent;
        _firstNopNodeName = firstNopNodeName;
        _lastNopNodeName = lastNopNodeName;
        _wasElseExecuted = false;
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

    /// <summary>
    /// After checking the previous condition(s) of the If Statement,
    /// conditionally executes some workflow code; equivalent to
    /// an if() statement in programming.
    /// </summary>
    /// <param name="condition">It is the WorkflowCondition to be satisfied.</param>
    /// <param name="body">It is the block of ThreadSpec code to be executed if the provided WorkflowCondition
    /// is satisfied.
    /// </param>
    /// <returns>WorkflowIfStatement</returns>
    public WorkflowIfStatement DoElseIf(WorkflowCondition condition, Action<WorkflowThread> body)
    {
        OrganizeEdgesAfterBodyExecution(body, condition);
        
        return this;
    }

    /// <summary>
    /// After checking all previous condition(s) of the If Statement,
    /// executes some workflow code; equivalent to
    /// an else block in programming.
    /// </summary>
    /// <param name="body">The block of ThreadSpec code to be executed if all previous
    ///  WorkflowConditions were not satisfied.
    /// </param>
    public void DoElse(Action<WorkflowThread> body)
    {
        if (_wasElseExecuted)
        {
            throw new InvalidOperationException("DoElse() method should not be called multiple times.");
        }
        
        _wasElseExecuted = true;
        OrganizeEdgesAfterBodyExecution(body);
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