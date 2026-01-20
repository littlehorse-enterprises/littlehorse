namespace LittleHorse.Sdk.Worker
{
  /// <summary>
  /// This class contains runtime information about the specific WfRun and NodeRun that is being
  /// executed by the Task Worker.It may optionally be added into the input parameters of your
  /// LHTaskMethod, and the Runtime will provision the WorkerContext and pass it into the method.
  /// </summary>
  public class LHCheckpointContext
  {
        /// <summary>
        /// The log output accumulated during this context.
        /// </summary>
        public string? LogOutput { get; private set; }

        /// <summary>
        /// Constructor for internal use by the Task Worker Library.
        /// </summary>
        public LHCheckpointContext()
        {
            LogOutput = "";
        }

        /// <summary>
        /// Stores log data for the given Checkpoint.
        /// </summary>
        /// <param name="item">The item to add to the Checkpoints's Log Output</param>
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