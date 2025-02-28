namespace LittleHorse.Sdk.Workflow.Spec;

public class NodeOutput
{
    public string NodeName { get; private set; }
    public WorkflowThread Parent { get; private set; }
    public string? JsonPath { get; private set; }

    public NodeOutput(string nodeName, WorkflowThread parent)
    {
        NodeName = nodeName;
        Parent = parent;
    }
    
    public NodeOutput WithJsonPath(string path) 
    {
        if (JsonPath != null) 
        {
            throw new Exception("Cannot use jsonpath() twice on same node!");
        }
        var nodeOutput = new NodeOutput(NodeName, Parent);
        nodeOutput.JsonPath = path;
        JsonPath = path;
        
        return nodeOutput;
    }
    
    public NodeOutput WithTimeout(int timeoutSeconds) 
    {
        Parent.AddTimeoutToExtEvt(this, timeoutSeconds);
        
        return this;
    }
}