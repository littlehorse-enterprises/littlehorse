using System.Text;
using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Helper
{
    public static class LHTaskHelper
    {
        internal static WfRunId? GetWfRunId(TaskRunSource taskRunSource)
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

        private static string ParseWfRunIdToString(WfRunId wfRunId) {
            var output = new StringBuilder();
            if (wfRunId.ParentWfRunId != null) {
                output.Append(ParseWfRunIdToString(wfRunId.ParentWfRunId));
                output.Append("_");
            }
            output.Append(wfRunId.Id);
            
            return output.ToString();
        }
        
        internal static string ParseTaskRunIdToString(TaskRunId taskRunId) {
            return ParseWfRunIdToString(taskRunId.WfRunId) + "/" + taskRunId.TaskGuid;
        }
    }
}
