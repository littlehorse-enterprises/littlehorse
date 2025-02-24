using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class TaskNodeOutput : NodeOutput
{
    public TaskNodeOutput(string nodeName, WorkflowThread parent) : base(nodeName, parent)
    {
    }
    
    public TaskNodeOutput WithExponentialBackoff(ExponentialBackoffRetryPolicy policy)
    {
        Parent.OverrideTaskExponentialBackoffPolicy(this, policy);
        return this;
    }
    
    public TaskNodeOutput WithRetries(int retries) 
    {
        Parent.OverrideTaskRetries(this, retries);
        return this;
    }
}