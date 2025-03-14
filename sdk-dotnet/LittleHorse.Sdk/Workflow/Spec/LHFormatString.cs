using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class LHFormatString
{
    public string Format { get; private set; }
    public List<VariableAssignment> Args { get; private set; }
    
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