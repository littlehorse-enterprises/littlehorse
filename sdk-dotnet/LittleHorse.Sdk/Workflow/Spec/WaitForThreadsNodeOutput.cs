using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WaitForThreadsNodeOutput: NodeOutput
{
    private readonly ThreadSpec _threadSpec;
    
    public WaitForThreadsNodeOutput(string nodeName, WorkflowThread parent, ThreadSpec threadSpec)
        : base(nodeName, parent)
    {
        _threadSpec = threadSpec;
    }
    
    /// <summary>
    /// Specifies a Failure Handler to run in case any of the ThreadRun's that we are waiting for in this
    /// WaitForThreadsNode fails with a specific EXCEPTION.
    /// </summary>
    /// <param name="exceptionName">
    /// It is the exception name
    /// </param>
    /// /// <param name="handler">
    /// The WorkflowThread defining the failure handler
    /// </param>
    /// <returns>This WaitForThreadsNodeOutput </returns>
    public WaitForThreadsNodeOutput HandleExceptionOnChild(string? exceptionName, Action<WorkflowThread> handler)
    {
        string threadName = "exn-handler-" + NodeName + "-"
                            + (exceptionName != null ? exceptionName : FailureHandlerDef.Types.LHFailureType.FailureTypeException);
        threadName = Parent.Parent.AddSubThread(threadName, handler);
        var handlerDef = new FailureHandlerDef
        {
            HandlerSpecName = threadName
        };
        if (exceptionName != null) {
            handlerDef.SpecificFailure = exceptionName;
        } else {
            handlerDef.AnyFailureOfType = FailureHandlerDef.Types.LHFailureType.FailureTypeException;
        }

        Parent.AddFailureHandlerOnWaitForThreadsNode(this, handlerDef);

        return this;
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
    public WaitForThreadsNodeOutput HandleErrorOnChild(LHErrorType? error, Action<WorkflowThread> handler)
    {
        string threadName = "error-handler-" + NodeName + "-"
                            + (error != null ? error : FailureHandlerDef.Types.LHFailureType.FailureTypeError);
        threadName = Parent.Parent.AddSubThread(threadName, handler);
        var handlerDef = new FailureHandlerDef { HandlerSpecName = threadName };
        if (error != null) 
        {
            handlerDef.SpecificFailure = error.ToString();
        } 
        else 
        {
            handlerDef.AnyFailureOfType = FailureHandlerDef.Types.LHFailureType.FailureTypeError;
        }

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