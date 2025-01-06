using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Exceptions;

public class LHTaskException: Exception
{
    public string Name { get; }

    public VariableValue Content { get; }
    
    public LHTaskException(String name, String message): base(message) 
    {
        Name = name;
        Content = new VariableValue();
    }

    public LHTaskException(String name, String message, VariableValue content): base(message)
    {
        Name = name;
        Content = content;
    }
}