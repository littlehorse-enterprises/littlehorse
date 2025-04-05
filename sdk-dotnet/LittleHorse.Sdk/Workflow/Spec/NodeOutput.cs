namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// <c>NodeOutput</c> represents the output of a Node execution. It can be used to set a timeout on a
/// node, or as input for a Variable Mutation.
/// </summary>
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
    
    /// <summary>
    /// Valid only for output of the JSON_OBJ or JSON_ARR types. Returns a new NodeOutput handle
    /// which points to Json element referred to by the json path.
    ///
    /// <p>This method is most often used to create the <c>rhs</c> parameter for
    /// <c>ThreadBuilder::Mutate()</c>. </p>
    ///
    /// <p>Can only be called once--you can't call <c>node.jsonPath().jsonPath().</c></p>
    /// </summary>
    /// <param name="path">
    /// Path is the json path to evaluate.
    /// </param>
    /// <returns>A NodeOutput.</returns>
    public NodeOutput WithJsonPath(string path) 
    {
        if (JsonPath != null) 
        {
            throw new Exception("Cannot use jsonpath() twice on same node!");
        }
        var nodeOutput = new NodeOutput(NodeName, Parent)
        {
            JsonPath = path
        };

        return nodeOutput;
    }
    
    /// <summary>
    /// Adds a timeout to a Node. Valid on TaskRuns and ExternalEvents.
    /// </summary>
    /// <param name="timeoutSeconds">
    /// The timeout length.
    /// </param>
    /// <returns>The NodeOutput.</returns>
    public NodeOutput WithTimeout(int timeoutSeconds) 
    {
        Parent.AddTimeoutToExtEvt(this, timeoutSeconds);
        
        return this;
    }
}