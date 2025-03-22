using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class UserTaskOutput : NodeOutput
{
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

    public UserTaskOutput WithNotes(string notes) 
    {
        AddNotes(notes);
        
        return this;
    }

    public UserTaskOutput WithNotes(WfRunVariable notes) 
    {
        AddNotes(notes);
        
        return this;
    }

    public UserTaskOutput WithNotes(LHFormatString notes) 
    {
        AddNotes(notes);
        
        return this;
    }
    
    public UserTaskOutput WithOnCancellationException(string exceptionName) 
    {
        AddOnCancellationException(exceptionName);
        
        return this;
    }
    
    public UserTaskOutput WithOnCancellationException(WfRunVariable exceptionName) 
    {
        AddOnCancellationException(exceptionName);
        
        return this;
    }
}