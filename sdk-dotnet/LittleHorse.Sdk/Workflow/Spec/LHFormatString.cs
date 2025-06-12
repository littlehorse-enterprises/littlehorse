using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;


/// <summary>
/// Represents a format string with a list of variable assignments.
/// </summary>
public class LHFormatString
{
    /// <value>
    /// The format string to be used.
    /// </value>
    public string Format { get; private set; }
    
    /// <value>
    /// The list of variable assignments to be used in the format string.
    /// </value>
    public List<VariableAssignment> Args { get; private set; }
    
    /// <summary>
    /// Initializes a new instance of the <see cref="LHFormatString"/> class with
    /// the specified format string and arguments.
    /// </summary>
    /// <param name="thread">The workflow thread where the string will be formatted.</param>
    /// <param name="format">The string format.</param>
    /// <param name="args">The arguments to be interpolated within the string format.</param>
    public LHFormatString(WorkflowThread thread, string format, object[] args) 
    {
        Format = format;
        Args = new List<VariableAssignment>();
        foreach (var arg in args)
        {
            Args.Add(thread.AssignVariableHelper(arg));
        }
    }
}