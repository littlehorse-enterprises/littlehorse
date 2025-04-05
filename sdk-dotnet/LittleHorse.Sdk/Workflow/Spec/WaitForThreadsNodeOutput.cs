using LittleHorse.Sdk.Common.Proto;
using static LittleHorse.Sdk.Common.Proto.FailureHandlerDef.Types;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// The `WaitForThreadsNodeOutput` class represents a specialized NodeOutput
/// used to manage parallel thread executions and control their behavior during
/// workflow execution.
///
/// When using this interface, you can set a policy that determines how the
/// node should handle waiting for threads' parallel executions:
/// 
/// </summary>
public class WaitForThreadsNodeOutput: NodeOutput
{
    public WaitForThreadsNodeOutput(string nodeName, WorkflowThread parent)
        : base(nodeName, parent)
    {
    }
    
    /// <summary>
    /// Specifies a Failure Handler to run in case any of the ThreadRun's that we are waiting for in this
    /// WaitForThreadsNode fails with a specific EXCEPTION.
    /// </summary>
    /// <param name="exceptionName">
    /// It is the exception name
    /// </param>
    /// <param name="handler">
    /// The WorkflowThread defining the failure handler
    /// </param>
    /// <returns>This WaitForThreadsNodeOutput </returns>
    public WaitForThreadsNodeOutput HandleExceptionOnChild(Action<WorkflowThread> handler, string? exceptionName=null)
    {
        string threadName = $"exn-handler-{NodeName}-" +
                            $"{exceptionName ?? LHConstants.FailureTypes[LHFailureType.FailureTypeException]}";
        threadName = Parent.Parent.AddSubThread(threadName, handler);
        var handlerDef = BuildFailureHandlerDef(threadName, exceptionName, LHFailureType.FailureTypeException);

        Parent.AddFailureHandlerOnWaitForThreadsNode(this, handlerDef);

        return this;
    }

    private FailureHandlerDef BuildFailureHandlerDef(string threadName, string? failureName, LHFailureType failureType)
    {
        var handlerDef = new FailureHandlerDef { HandlerSpecName = threadName };
        if (!string.IsNullOrEmpty(failureName)) 
        {
            handlerDef.SpecificFailure = failureName;
        } 
        else 
        {
            handlerDef.AnyFailureOfType = failureType;
        }

        return handlerDef;
    }

    /// <summary>
    /// Specifies a Failure Handler to run in case any of the ThreadRun's that we are waiting for in this
    /// WaitForThreadsNode fails with a specific ERROR.
    /// </summary>
    /// <param name="error">
    /// It is the ERROR type
    /// </param>
    /// /// <param name="handler">
    /// The WorkflowThread defining the failure handler
    /// </param>
    /// <returns>This WaitForThreadsNodeOutput. </returns>
    public WaitForThreadsNodeOutput HandleErrorOnChild(Action<WorkflowThread> handler, LHErrorType? error=null)
    {
        string failureName = error != null ? LHConstants.ErrorTypes[error.ToString()!] : string.Empty;
        string threadName = $"error-handler-{NodeName}-" +
                            $"{(error != null ? failureName : LHConstants.FailureTypes[LHFailureType.FailureTypeError])}";
        threadName = Parent.Parent.AddSubThread(threadName, handler);
        var handlerDef = BuildFailureHandlerDef(threadName, failureName, LHFailureType.FailureTypeError);
        Parent.AddFailureHandlerOnWaitForThreadsNode(this, handlerDef);

        return this;
    }
    
    /// <summary>
    /// Specifies a Failure Handler to run in case any of the ThreadRun's that we are waiting for in this
    /// WaitForThreadsNode fails with any Failure.
    /// </summary>
    /// /// <param name="handler">
    /// The WorkflowThread defining the failure handler
    /// </param>
    /// <returns>This WaitForThreadsNodeOutput. </returns>
    public WaitForThreadsNodeOutput HandleAnyFailureOnChild(Action<WorkflowThread> handler)
    {
        string threadName = "failure-handler-" +NodeName + "-ANY_FAILURE";
        threadName = Parent.Parent.AddSubThread(threadName, handler);
        var handlerDef = new FailureHandlerDef { HandlerSpecName = threadName };
        Parent.AddFailureHandlerOnWaitForThreadsNode(this, handlerDef);
        
        return this;
    }
}