using LittleHorse.Common.Proto;

namespace LittleHorse.Sdk.Exceptions;

public class LHTaskException: Exception
{
    private String name;
    private VariableValue content;

    public LHTaskException(String name, String message): base(message) 
    {
        this.name = name;
        this.content = null;
    }

    public LHTaskException(String name, String message, VariableValue content): base(message)
    {
        this.name = name;
        this.content = content;
    }
}