using LittleHorse.Common.Proto;

namespace LittleHorse.Sdk.Worker
{
    /// <summary>
    /// This class contains runtime information about the specific WfRun and NodeRun that is being
    /// executed by the Task Worker.It may optionally be added into the input parameters of your
    /// LHTaskMethod, and the Runtime will provision the WorkerContext and pass it into the method.
    /// </summary>
    public class LHWorkerContext
    {
        private DateTime? _scheduleDateTime;
        private ScheduledTask _scheduleTask;

        public string? LogOutput { get; private set; }

        /// <summary>
        /// Constructor for internal use by the Task Worker Library.
        /// </summary>
        /// <param name="scheduleTask">The raw payload for the scheduled task.</param>
        /// <param name="scheduleDateTime">The time that the task was actually scheduled.</param>
        public LHWorkerContext(ScheduledTask scheduleTask, DateTime? scheduleDateTime)
        {
            _scheduleTask = scheduleTask;
            _scheduleDateTime = scheduleDateTime;
        }

        /// <summary>
        /// Provides a way to push data into the log output. Any object may be passed in; its string
        /// representation will be appended to the logOutput of this NodeRun.
        /// </summary>
        /// <param name="item">The Object to log to the NodeRun's logOutput.</param>
        public void Log(object item)
        {
            if (item != null)
            {
                LogOutput += item.ToString();
            }
            else
            {
                LogOutput += "null";
            }
        }


    }
}
