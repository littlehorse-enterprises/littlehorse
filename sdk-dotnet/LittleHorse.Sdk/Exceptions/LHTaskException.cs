using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Exceptions;

/// <summary>
/// Maps an Exception that contains a client specific error in a Task Method.
/// </summary>
public class LHTaskException: Exception
{
    /// <value>Property <c>Name</c> represents the name of the exception.</value>
    public string Name { get; }
    
    /// <value>Property <c>Content</c> represents the content of the exception in LH language.</value>
    public VariableValue Content { get; }
    
    /// <summary>
    /// Constructor of the Exception which contains a problem in Task Method.
    /// </summary>
    /// <param name="name"> The name of the exception.</param>
    /// <param name="message"> A custom message.</param>
    public LHTaskException(string name, string message): base(message) 
    {
        Name = name;
        Content = new VariableValue();
    }

    /// <summary>
    /// Constructor of the Exception which contains a problem in Task Method.
    /// </summary>
    /// <param name="name"> The name of the exception.</param>
    /// <param name="message"> A custom message.</param>
    /// <param name="content"> The content of the exception in LH language.</param>
    public LHTaskException(string name, string message, VariableValue content): base(message)
    {
        Name = name;
        Content = content;
    }
}