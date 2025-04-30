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
    /// Executes the body of the if statement if the condition is true.
    /// </summary>
    /// <param name="condition">The workflow condition to be validated.</param>
    /// <param name="body">The body function to execute.
    /// When the condition is true, the internal function business logic will be executed.
    /// </param>
    /// <returns>WorkflowIfStatement</returns>
    public WorkflowIfStatement DoElseIf(WorkflowCondition condition, Action<WorkflowThread> body)
    {
        OrganizeEdgesAfterBodyExecution(body, condition);
        
        return this;
    }

    /// <summary>
    /// Executes the body of the else statement if the condition is false.
    /// </summary>
    /// <param name="body">The body function to execute.</param>
    /// When the condition is false, the internal function business logic will be executed.
    /// <returns>WorkflowIfStatement</returns>
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