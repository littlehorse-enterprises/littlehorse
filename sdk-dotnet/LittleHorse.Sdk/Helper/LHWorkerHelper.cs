using LittleHorse.Common.Proto;

namespace LittleHorse.Sdk.Helper
{
    public static class LHWorkerHelper
    {
        public static string? GetWFRunId(TaskRunSource taskRunSource)
        {
            switch (taskRunSource.TaskRunSourceCase)
            {
                case TaskRunSource.TaskRunSourceOneofCase.TaskNode:
                    return taskRunSource.TaskNode.NodeRunId.WfRunId.ToString();
                case TaskRunSource.TaskRunSourceOneofCase.UserTaskTrigger:
                    return taskRunSource.UserTaskTrigger.NodeRunId.WfRunId.ToString();
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
