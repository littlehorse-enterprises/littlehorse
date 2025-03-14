using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class UserTaskOutput : NodeOutput
{
    private VariableAssignment _notes;
    
    public UserTaskOutput(string nodeName, WorkflowThread parent): base(nodeName, parent)
    {
    }
    
    public VariableAssignment GetNotes()
    {
        return _notes;
    }

    private void AddNotes(object notes) 
    {
        VariableAssignment notesParsed = Parent.AssignVariableHelper(notes);
        
        Node node = Parent.FindNode(NodeName);
        node.UserTask.Notes =  notesParsed;
        Parent.Compile().Nodes.Add(NodeName, node);
    }

    private void AddOnCancellationException(object exceptionName) 
    {
        VariableAssignment exceptionNameParsed = Parent.AssignVariableHelper(exceptionName);

        // get the Node
        Node node = Parent.FindNode(NodeName);
        node.UserTask.OnCancellationExceptionName = exceptionNameParsed;
        Parent.Compile().Nodes.Add(NodeName, node);
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