using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents the output of a user task in a workflow.
/// </summary>
public class UserTaskOutput : NodeOutput
{
    /// <summary>
    /// Initializes a new instance of the <see cref="UserTaskOutput"/> class.
    /// </summary>
    /// <param name="nodeName">The specified node name.</param>
    /// <param name="parent">The workflow thread where the user task output belongs to.</param>
    public UserTaskOutput(string nodeName, WorkflowThread parent): base(nodeName, parent)
    {
    }

    private void AddNotes(object notes) 
    {
        VariableAssignment notesParsed = Parent.AssignVariableHelper(notes);
        
        Node node = Parent.FindNode(NodeName);
        node.UserTask.Notes =  notesParsed;
    }

    private void AddOnCancellationException(object exceptionName) 
    {
        VariableAssignment exceptionNameParsed = Parent.AssignVariableHelper(exceptionName);

        Node node = Parent.FindNode(NodeName);
        node.UserTask.OnCancellationExceptionName = exceptionNameParsed;
    }

    /// <summary>
    /// Adds notes to the user task output.
    /// </summary>
    /// <param name="notes">Notes to be added.</param>
    /// <returns>A UserTaskOutput</returns>
    public UserTaskOutput WithNotes(string notes) 
    {
        AddNotes(notes);
        
        return this;
    }

    /// <summary>
    /// Adds notes to the user task output.
    /// </summary>
    /// <param name="notes">Notes to be added.</param>
    /// <returns>A UserTaskOutput</returns>
    public UserTaskOutput WithNotes(WfRunVariable notes) 
    {
        AddNotes(notes);
        
        return this;
    }

    /// <summary>
    /// Adds notes to the user task output.
    /// </summary>
    /// <param name="notes">Notes to be added.</param>
    /// <returns>A UserTaskOutput</returns>
    public UserTaskOutput WithNotes(LHFormatString notes) 
    {
        AddNotes(notes);
        
        return this;
    }
    
   
    /// <summary>
    /// Adds an exception name to be used when the user task is cancelled.
    /// </summary>
    /// <param name="exceptionName">The exception name.</param>
    /// <returns>A UserTaskOutput</returns>
    public UserTaskOutput WithOnCancellationException(string exceptionName) 
    {
        AddOnCancellationException(exceptionName);
        
        return this;
    }
    
    /// <summary>
    /// Adds an exception name to be used when the user task is cancelled.
    /// </summary>
    /// <param name="exceptionName">The exception name.</param>
    /// <returns>A UserTaskOutput</returns>
    public UserTaskOutput WithOnCancellationException(WfRunVariable exceptionName) 
    {
        AddOnCancellationException(exceptionName);
        
        return this;
    }
}