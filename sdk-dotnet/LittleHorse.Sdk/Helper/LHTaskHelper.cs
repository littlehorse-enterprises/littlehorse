using System.Text;
using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Helper
{
    internal static class LHTaskHelper
    {
        internal static WfRunId? GetWfRunId(TaskRunSource taskRunSource)
        {
            return taskRunSource.TaskRunSourceCase switch
            {
                TaskRunSource.TaskRunSourceOneofCase.TaskNode => taskRunSource.TaskNode.NodeRunId.WfRunId,
                TaskRunSource.TaskRunSourceOneofCase.UserTaskTrigger => taskRunSource.UserTaskTrigger.NodeRunId.WfRunId,
                _ => null
            };
        }

        private static string ParseWfRunIdToString(WfRunId wfRunId)
        {
            var output = new StringBuilder();
            if (wfRunId.ParentWfRunId != null) {
                output.Append(ParseWfRunIdToString(wfRunId.ParentWfRunId));
                output.Append("_");
            }
            output.Append(wfRunId.Id);

            return output.ToString();
        }

        internal static string ParseTaskRunIdToString(TaskRunId taskRunId)
        {
            return ParseWfRunIdToString(taskRunId.WfRunId) + "/" + taskRunId.TaskGuid;
        }
    }
}
