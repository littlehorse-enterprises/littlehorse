using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk.Worker
{
    /// <summary>
    /// This class contains runtime information about the specific WfRun and NodeRun that is being
    /// executed by the Task Worker.It may optionally be added into the input parameters of your
    /// LHTaskMethod, and the Runtime will provision the WorkerContext and pass it into the method.
    /// </summary>
    public class LHWorkerContext
    {
        private readonly DateTime? _scheduleDateTime;
        private readonly ScheduledTask _scheduleTask;

        private int _checkpointsSoFarInThisRun;

        private readonly LittleHorseClient _client;

        /// <summary>
        /// The current logOutput.
        /// </summary>
        public string? LogOutput { get; private set; }

        /// <summary>
        /// Constructor for internal use by the Task Worker Library.
        /// </summary>
        /// <param name="scheduleTask">The raw payload for the scheduled task.</param>
        /// <param name="scheduleDateTime">The time that the task was actually scheduled.</param>
        /// <param name="client">A LittleHorse Client for putting and fetching Checkpoints.</param>
        public LHWorkerContext(ScheduledTask scheduleTask, DateTime? scheduleDateTime, LittleHorseClient client)
        {
            _scheduleTask = scheduleTask;
            _scheduleDateTime = scheduleDateTime;
            _checkpointsSoFarInThisRun = 0;
            _client = client;
        }
        
        /// <summary>
        /// Returns the Id of the WfRun for the NodeRun that's being executed.
        /// </summary>
        /// <returns>
        /// the Id of the WfRun for the NodeRun that's being executed.
        /// </returns>
        [Obsolete("GetWfRunId is deprecated, please use WfRunId property instead.")]
        public WfRunId GetWfRunId() 
        {
            return WfRunId;
        }
        
        /// <summary>
        /// Returns the Id of the WfRun for the NodeRun that's being executed.
        /// </summary>
        /// <returns>
        /// the Id of the WfRun for the NodeRun that's being executed.
        /// </returns>
        public WfRunId WfRunId => LHTaskHelper.GetWfRunId(_scheduleTask.Source)!;

        /// <summary>
        /// Returns the NodeRun ID for the Task that was just scheduled.
        /// </summary>
        /// <returns>
        /// A <c>NodeRunIdPb</c> protobuf class with the ID from the executed NodeRun.
        /// </returns>
        [Obsolete("GetNodeRunId is deprecated, please use NodeRunId property instead.")]
        public NodeRunId? GetNodeRunId()
        {
            return NodeRunId;
        }

        /// <summary>
        /// Returns the NodeRun ID for the Task that was just scheduled.
        /// </summary>
        /// <returns>
        /// A <c>NodeRunIdPb</c> protobuf class with the ID from the executed NodeRun.
        /// </returns>
        public NodeRunId? NodeRunId
        {
            get
            {
                var source = _scheduleTask.Source;
                return source.TaskRunSourceCase switch
                {
                    TaskRunSource.TaskRunSourceOneofCase.TaskNode => source.TaskNode.NodeRunId,
                    TaskRunSource.TaskRunSourceOneofCase.UserTaskTrigger => source.UserTaskTrigger.NodeRunId,
                    _ => null
                };
            }
        }
        
        /// <summary>
        /// Returns the attemptNumber of the NodeRun that's being executed. If this is the first attempt,
        /// returns zero. If this is the first retry, returns 1, and so on.
        /// </summary>
        /// <returns>
        /// The attempt number of the NodeRun that's being executed.
        /// </returns>
        [Obsolete("GetAttemptNumber is deprecated, please use AttemptNumber property instead.")]
        public int GetAttemptNumber() 
        {
            return AttemptNumber;
        }
        
        /// <summary>
        /// Returns the attemptNumber of the NodeRun that's being executed. If this is the first attempt,
        /// returns zero. If this is the first retry, returns 1, and so on.
        /// </summary>
        /// <returns>
        /// The attempt number of the NodeRun that's being executed.
        /// </returns>
        public int AttemptNumber => _scheduleTask.AttemptNumber;

        /// <summary>
        /// Returns the time at which the task was scheduled by the processor. May be useful in certain
        /// customer edge cases, eg. to determine whether it's too late to actually perform an action,
        /// when (now() - getScheduledTime()) is above some threshold, etc.
        /// </summary>
        /// <returns>
        /// The time at which the current NodeRun was scheduled.
        /// </returns>
        [Obsolete("GetScheduledTime is deprecated, please use ScheduledTime property instead.")]
        public DateTime? GetScheduledTime() 
        {
            return ScheduledTime;
        }

        /// <summary>
        /// Returns the time at which the task was scheduled by the processor. May be useful in certain
        /// customer edge cases, eg. to determine whether it's too late to actually perform an action,
        /// when (now() - ScheduledTime) is above some threshold, etc.
        /// </summary>
        /// <returns>
        /// The time at which the current NodeRun was scheduled.
        /// </returns>
        public DateTime? ScheduledTime => _scheduleDateTime;

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
        
        /// <summary>
        /// Returns the TaskRunId of this TaskRun.
        /// </summary>
        /// <returns>
        /// The associated TaskRunId.
        /// </returns>
        [Obsolete("GetTaskRunId is deprecated, please use TaskRunId property instead.")]
        public TaskRunId GetTaskRunId()
        {
            return TaskRunId;
        }

        /// <summary>
        /// Returns the TaskRunId of this TaskRun.
        /// </summary>
        /// <returns>
        /// The associated TaskRunId.
        /// </returns>
        public TaskRunId TaskRunId => _scheduleTask.TaskRunId;

        private UserTaskTriggerReference? GetUserTaskTrigger() 
        {
            return _scheduleTask.Source.UserTaskTrigger;
        }
        
        /// <summary>
        /// If this TaskRun is a User Task Reminder TaskRun, then this method returns the
        /// UserId of the user who the associated UserTask is assigned to. Returns
        /// null if:
        /// - this TaskRun is not a Reminder Task
        /// - this TaskRun is a Reminder Task, but the UserTaskRun does not have an assigned
        ///   user id.
        /// </summary>
        /// <returns>
        ///The id of the user that the associated UserTask is assigned to.
        /// </returns>
        [Obsolete("GetUserId is deprecated, please use UserId property instead.")]
        public string? GetUserId()
        {
            return UserId;
        }

        /// <summary>
        /// If this TaskRun is a User Task Reminder TaskRun, then this method returns the
        /// UserId of the user who the associated UserTask is assigned to. Returns
        /// null if:
        /// - this TaskRun is not a Reminder Task
        /// - this TaskRun is a Reminder Task, but the UserTaskRun does not have an assigned
        ///   user id.
        /// </summary>
        /// <returns>
        /// The id of the user that the associated UserTask is assigned to.
        /// </returns>
        public string? UserId
        {
            get
            {
                var userTaskTrigger = GetUserTaskTrigger();
                if (userTaskTrigger == null) return null;
                return userTaskTrigger.HasUserId ? userTaskTrigger.UserId : null;
            }
        }
        
        /// <summary>
        /// If this TaskRun is a User Task Reminder TaskRun, then this method returns the
        /// UserGroup that the associated UserTask is assigned to. Returns null if:
        /// - this TaskRun is not a Reminder Task
        /// - this TaskRun is a Reminder Task, but the UserTaskRun does not have an
        ///   associated User Group
        /// </summary>
        /// <returns>
        ///The id of the User Group that the associated UserTask is assigned to.
        /// </returns>
        [Obsolete("GetUserGroup is deprecated, please use UserGroup property instead.")]
        public string? GetUserGroup() {
            return UserGroup;
        }

        /// <summary>
        /// If this TaskRun is a User Task Reminder TaskRun, then this method returns the
        /// UserGroup that the associated UserTask is assigned to. Returns null if:
        /// - this TaskRun is not a Reminder Task
        /// - this TaskRun is a Reminder Task, but the UserTaskRun does not have an
        ///   associated User Group
        /// </summary>
        /// <returns>
        ///The id of the User Group that the associated UserTask is assigned to.
        /// </returns>
        public string? UserGroup
        {
            get
            {
                var userTaskTrigger = GetUserTaskTrigger();
                if (userTaskTrigger == null) return null;
                return userTaskTrigger.HasUserGroup ? userTaskTrigger.UserGroup : null;
            }
        }
        
        /// <summary>
        /// Returns an idempotency key that can be used to make calls to upstream api's idempotent across
        /// TaskRun Retries.
        /// </summary>
        /// <returns>
        /// An idempotency key.
        /// </returns>
        [Obsolete("GetIdempotencyKey is deprecated, please use IdempotencyKey property instead.")]
        public string GetIdempotencyKey() 
        {
            return IdempotencyKey;
        }

        /// <summary>
        /// Returns an idempotency key that can be used to make calls to upstream api's idempotent across
        /// TaskRun Retries
        /// </summary>
        public string IdempotencyKey => LHTaskHelper.ParseTaskRunIdToString(TaskRunId);

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="checkpointContext"></param>
        /// <returns></returns>
        public delegate T CheckpointableFunction<T>(LHCheckpointContext checkpointContext);

        /// <summary>
        /// Executes and checkpoints the result of a function. 
        /// 
        /// On first Checkpoint Attempt, the Task Worker will execute you CheckpointableFunction
        /// and put the result to the server as a Checkpoint.
        /// 
        /// If your overall Task Attempt fails after your Checkpoint, this method will
        /// retrieve the Checkpoint from the server on future iterations.
        /// </summary>
        /// <typeparam name="T">The return type of your checkpoint</typeparam>
        /// <param name="func">Your checkpointable function</param>
        /// <returns>The function result</returns>
        public T? ExecuteAndCheckpoint<T>(CheckpointableFunction<T> func)
        {
            if (_checkpointsSoFarInThisRun < _scheduleTask.TotalObservedCheckpoints)
            {
                return FetchCheckpoint<T>(_checkpointsSoFarInThisRun++);
            }
            else
            {
                return SaveCheckpoint(func);
            }
        }

        /// <summary>
        /// Fetches a checkpoint from the server based on its Checkpoint order number.
        /// </summary>
        /// <typeparam name="T">The type of your checkpoint data</typeparam>
        /// <param name="checkpointNumber">The Checkpoint number</param>
        /// <returns>The Checkpoint data</returns>
        private T? FetchCheckpoint<T>(int checkpointNumber)
        {
            CheckpointId id = new()
            {
                TaskRun = _scheduleTask.TaskRunId,
                CheckpointNumber = checkpointNumber,
            };

            Checkpoint checkpoint = _client.GetCheckpoint(id);

            object? obj = LHMappingHelper.VariableValueToObject(checkpoint.Value, typeof(T));

            if (obj == null)
            {
                return default;
            }

            return (T) obj;
        }

        /// <summary>
        /// Executes a Checkpointable Function and puts the result to the server
        /// </summary>
        /// <typeparam name="T">The return type of the Checkpointable Function</typeparam>
        /// <param name="func">The Checkpointable Function</param>
        /// <returns>The result of the Checkpointable Function</returns>
        /// <exception cref="Exception">
        /// Throws an Exception if the server halts Task Worker progress in the PutCheckpointResponse
        /// </exception>
        private T SaveCheckpoint<T>(CheckpointableFunction<T> func)
        {
            LHCheckpointContext checkpointContext = new();
            T result = func(checkpointContext);

            PutCheckpointResponse response = _client.PutCheckpoint(new PutCheckpointRequest
            {
               TaskAttempt = _scheduleTask.AttemptNumber,
               TaskRunId = _scheduleTask.TaskRunId,
               Value = LHMappingHelper.ObjectToVariableValue(result),
               Logs = checkpointContext.LogOutput
            });

            _checkpointsSoFarInThisRun++;

            if (response.FlowControlContinueType != PutCheckpointResponse.Types.FlowControlContinue.ContinueTask)
            {
                throw new Exception("Halting execution because the server told us to.");
            }
            
            return result;
        }

    }
}
