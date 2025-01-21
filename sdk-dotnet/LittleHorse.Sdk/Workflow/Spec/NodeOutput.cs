namespace LittleHorse.Sdk.Workflow.Spec;

public class NodeOutput
{
    public string NodeName { get; set; }
    public WorkflowThread Parent { get; set; }
    public string? JsonPath { get; set; }

    public NodeOutput(string nodeName, WorkflowThread parent)
    {
        NodeName = nodeName;
        Parent = parent;
    }
    
    public NodeOutput WithJsonPath(String path) 
    {
        if (JsonPath != null) 
        {
            throw new Exception("Cannot use jsonpath() twice on same node!");
        }
        var nodeOutput = new NodeOutput(NodeName, Parent);
        JsonPath = path;
        
        return nodeOutput;
    }
}