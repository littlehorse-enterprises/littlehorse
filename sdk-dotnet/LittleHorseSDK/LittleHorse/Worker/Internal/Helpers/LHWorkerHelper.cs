using LittleHorse.Common.Proto;

namespace LittleHorse.Worker.Internal.Helpers
{
    public static class LHWorkerHelper
    {
        public static string? GetWFRunId(TaskRunSource taskRunSource)
        {
            switch (taskRunSource.TaskRunSourceCase)
            {
                case TaskRunSource.TaskRunSourceOneofCase.TaskNode:
                    return taskRunSource.TaskNode.NodeRunId.WfRunId;
                case TaskRunSource.TaskRunSourceOneofCase.UserTaskTrigger:
                    return taskRunSource.UserTaskTrigger.NodeRunId.WfRunId;
                default:
                    return null;
            }
        }

        public static string TaskRunIdToString(TaskRunId taskRunId)
        {
            return $"{taskRunId.WfRunId}/{taskRunId.TaskGuid}";
        }
    }
}
