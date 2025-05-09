namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a conditional statement in a workflow.
/// </summary>
public class WorkflowIfStatement
{
    private readonly WorkflowThread _parent;
    internal string FirstNopNodeName { get; private set; }
    internal string LastNopNodeName { get; private set; }
    private bool _wasElseExecuted;
    
    internal WorkflowIfStatement(WorkflowThread parent, string firstNopNodeName, string lastNopNodeName)
    {
        _parent = parent;
        FirstNopNodeName = firstNopNodeName;
        LastNopNodeName = lastNopNodeName;
        _wasElseExecuted = false;
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
        _parent.OrganizeEdgesForElseIfExecution(this, body, condition);
        
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
            throw new InvalidOperationException("Else block has already been executed. Cannot add another else block.");
        }
        
        _wasElseExecuted = true;
        _parent.OrganizeEdgesForElseIfExecution(this, body);
    }
}