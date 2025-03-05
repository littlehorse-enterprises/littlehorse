using LittleHorse.Sdk.Common.Proto;
using static LittleHorse.Sdk.Common.Proto.FailureHandlerDef.Types;

namespace LittleHorse.Sdk;

internal static class LHConstants
{
    internal static readonly Dictionary<Node.NodeOneofCase, string> NodeTypes = new()
    {
        { Node.NodeOneofCase.Entrypoint, "ENTRYPOINT" },
        { Node.NodeOneofCase.Exit, "EXIT" },
        { Node.NodeOneofCase.Task, "TASK" },
        { Node.NodeOneofCase.None, "NONE" },
        { Node.NodeOneofCase.ExternalEvent, "EXTERNAL_EVENT" },
        { Node.NodeOneofCase.StartThread, "START_THREAD" },
        { Node.NodeOneofCase.Nop, "NOP" },
        { Node.NodeOneofCase.Sleep, "SLEEP" },
        { Node.NodeOneofCase.UserTask, "USER_TASK" },
        { Node.NodeOneofCase.StartMultipleThreads, "START_MULTIPLE_THREADS" },
        { Node.NodeOneofCase.ThrowEvent, "THROW_EVENT" },
        { Node.NodeOneofCase.WaitForCondition, "WAIT_FOR_CONDITION" },
        { Node.NodeOneofCase.WaitForThreads, "WAIT_FOR_THREADS" }
    };

    internal static readonly Dictionary<string, string> ErrorTypes = new()
    {
        { "ChildFailure", "CHILD_FAILURE" },
        { "VarSubError", "VAR_SUB_ERROR" },
        { "VarMutationError", "VAR_MUTATION_ERROR" },
        { "UserTaskCancelled", "USER_TASK_CANCELLED" },
        { "LHErrorType.Timeout", "TIMEOUT" },
        { "TaskFailure", "TASK_FAILURE" },
        { "VarError", "VAR_ERROR" },
        { "TaskError", "TASK_ERROR" },
        { "InternalError", "INTERNAL_ERROR" }
    };
    
    internal static readonly Dictionary<LHFailureType, string> FailureTypes = new()
    {
        { LHFailureType.FailureTypeError, "FAILURE_TYPE_ERROR" },
        { LHFailureType.FailureTypeException, "FAILURE_TYPE_EXCEPTION" }
    };
}