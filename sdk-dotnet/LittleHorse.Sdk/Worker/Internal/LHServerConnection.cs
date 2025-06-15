using System.Data;
using System.Reflection;
using Google.Protobuf.WellKnownTypes;
using Grpc.Core;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using Microsoft.Extensions.Logging;
using Polly;
using Polly.Retry;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;
using LHTaskException = LittleHorse.Sdk.Exceptions.LHTaskException;
using TaskStatus = LittleHorse.Sdk.Common.Proto.TaskStatus;

namespace LittleHorse.Sdk.Worker.Internal
{
    /// <summary>
    /// Represents a connection to the LH server for a specific task type.
    /// </summary>
    /// <typeparam name="T">The type of the task.</typeparam>
    internal class LHServerConnection<T>
    {
        private const int ReportTaskRetriesIntervalSeconds = 2;
        private const int MaxReportRetries = 15;
        private const int PollTaskSleepTime = 5000;
        
        /// <summary>
        /// Needs to be volatile because it's accessed from other tasks
        /// </summary>
        private volatile bool _running;

        private readonly LittleHorseClient _client;
        private readonly AsyncDuplexStreamingCall<PollTaskRequest, PollTaskResponse> _pollTask;
        private readonly ILogger? _logger;
        private readonly LHTask<T> _task;
        private readonly PollTaskRequest _pollTaskRequest;
        private readonly CancellationToken _cancellationToken;

        /// <summary>
        /// Creates a new instance of the <see cref="LHServerConnection{T}"/> class.
        /// </summary>
        /// <param name="clientId"></param>
        /// <param name="taskDefId"></param>
        /// <param name="taskWorkerVersion">Version of the Worker</param>
        /// <param name="client"></param>
        /// <param name="task">Task method to be executed</param>
        /// <param name="cancellationToken"></param>
        internal LHServerConnection(
            string clientId,
            TaskDefId taskDefId,
            string taskWorkerVersion,
            LHTask<T> task,
            LittleHorseClient client,
            CancellationToken cancellationToken)
        {
            _pollTaskRequest = new PollTaskRequest
            {
                ClientId = clientId,
                TaskDefId = taskDefId,
                TaskWorkerVersion = taskWorkerVersion
            };
            _logger = LHLoggerFactoryProvider.GetLogger<LHServerConnection<T>>();
            _client = client;
            _pollTask = client.PollTask();
            _task = task;
            _cancellationToken = cancellationToken;
        }

        /// <summary>
        /// Starts a new async task which will poll the server for tasks to execute.
        /// </summary>
        internal async Task Start()
        {
            _running = true;

            try
            {
                await RequestWorkLoop();
            }
            catch (OperationCanceledException)
            {
                _logger?.LogDebug("Connection cancelled");
            }
            catch (Exception exception)
            {
                _logger?.LogError(exception, "Connection error. Stopping connection: {}", exception.Message);
            }

            _running = false;
        }

        internal bool IsRunning()
        {
            return _running;
        }

        private async Task RequestWorkLoop()
        {
            while (!_cancellationToken.IsCancellationRequested)
            {
                await _pollTask.RequestStream.WriteAsync(_pollTaskRequest, _cancellationToken);
                if (!await _pollTask.ResponseStream.MoveNext(_cancellationToken)) continue;

                var response = _pollTask.ResponseStream.Current;
                var scheduledTask = response.Result;

                if (scheduledTask != null)
                {
                    await DoTask(scheduledTask);
                }
                else
                {
                    _logger?.LogWarning("Didn't successfully claim task, likely due to a server restart.");
                    await Task.Delay(PollTaskSleepTime);
                }
            }

            await _pollTask.RequestStream.CompleteAsync();
        }
      
        private async Task DoTask(ScheduledTask scheduledTask)
        {
            var wfRunId = LHTaskHelper.GetWfRunId(scheduledTask.Source);

            _logger?.LogDebug("Received task schedule request for wfRun {}", wfRunId?.Id);

            ReportTaskRun result = await ExecuteTask(scheduledTask, LHMappingHelper.DateTimeFromProtoTimeStamp(scheduledTask.CreatedAt));

            _logger?.LogDebug("Task {} successfully executed for wfRun {}", scheduledTask.TaskRunId.TaskGuid, wfRunId?.Id);

            await ReportTaskWithRetries(result, wfRunId);
        }

        private async Task ReportTaskWithRetries(ReportTaskRun result, WfRunId? wfRunId)
        {
            try
            {
                _logger?.LogDebug("Starting task reporting for wfRun {} and TaskRunId {}.", wfRunId?.Id, result.TaskRunId.TaskGuid);
                await RetryPolicy(result, wfRunId, MaxReportRetries);
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, "Failed to report task for wfRun {}: {}. No retries left.",  wfRunId, ex.Message);
            }
        }

        private Task RetryPolicy(ReportTaskRun result, WfRunId? wfRunId, int maxRetries) =>
            Policy
                .Handle<Exception>()
                .WaitAndRetryAsync(
                    retryCount: maxRetries,
                    sleepDurationProvider: _ => TimeSpan.FromSeconds(ReportTaskRetriesIntervalSeconds),
                    onRetry: (exception, _, retryCount, _) =>
                    {
                        _logger?.LogDebug(exception, "Retry attempt {} failed for wfRun {} and TaskRunId {}. Exception: {}. Retries left: {}", retryCount, wfRunId, result.TaskRunId.TaskGuid, exception.Message, maxRetries - retryCount);
                        _logger?.LogDebug("Retrying reportTask rpc on taskRun {}/{}", result.TaskRunId.WfRunId, result.TaskRunId.TaskGuid);
                    })
                .ExecuteAsync(() => RunReportTask(result));

        private async Task RunReportTask(ReportTaskRun reportedTask)
        {
            await _client.ReportTaskAsync(reportedTask);
        }

        private async Task<ReportTaskRun> ExecuteTask(ScheduledTask scheduledTask, DateTime? scheduleTime)
        {
            var taskResult = new ReportTaskRun
            {
                TaskRunId = scheduledTask.TaskRunId,
                AttemptNumber = scheduledTask.AttemptNumber
            };

            var workerContext = new LHWorkerContext(scheduledTask, scheduleTime);

            try
            {
                var result = await Invoke(scheduledTask, workerContext);
                var serialized = LHMappingHelper.ObjectToVariableValue(result);
                taskResult.Output = serialized;
                taskResult.Status = TaskStatus.TaskSuccess;

                if (!string.IsNullOrEmpty(workerContext.LogOutput))
                {
                    var outputVariable = new VariableValue
                    {
                        Str = workerContext.LogOutput
                    };

                    taskResult.LogOutput = outputVariable;
                }

            }
            catch (LHInputVarSubstitutionException ex)
            {
                _logger?.LogError(ex, "Failed calculating task input variables");
                taskResult.LogOutput = LHMappingHelper.ExceptionToVariableValue(ex, workerContext);
                taskResult.Status = TaskStatus.TaskInputVarSubError;
                taskResult.Error = new LHTaskError
                {
                    Message = ex.ToString(), Type = LHMappingHelper.GetFailureCodeFor(taskResult.Status)
                };
            }
            catch (LHSerdeException ex)
            {
                _logger?.LogError(ex, "Failed serializing Task Output");
                taskResult.LogOutput = LHMappingHelper.ExceptionToVariableValue(ex, workerContext);
                taskResult.Status = TaskStatus.TaskOutputSerdeError;
                taskResult.Error = new LHTaskError
                {
                    Message = ex.ToString(), Type = LHMappingHelper.GetFailureCodeFor(taskResult.Status)
                };
            }
            catch (TargetInvocationException ex)
            {
                if (ex.GetBaseException() is LHTaskException taskException)
                {
                    _logger?.LogError(ex, "Task Method threw a Business Exception");
                    taskResult.LogOutput = LHMappingHelper.ExceptionToVariableValue(ex, workerContext);
                    taskResult.Status = TaskStatus.TaskException;
                    taskResult.Exception = new Common.Proto.LHTaskException
                    {
                        Name = taskException.Name, 
                        Message = taskException.Message, 
                        Content = taskException.Content
                    };
                }
                else
                {
                    _logger?.LogError(ex, "Task Method threw an exception");
                    taskResult.LogOutput = LHMappingHelper.ExceptionToVariableValue(ex, workerContext);
                    taskResult.Status = TaskStatus.TaskFailed;
                    taskResult.Error = new LHTaskError
                    {
                        Message = ex.InnerException!.ToString(), 
                        Type = LHMappingHelper.GetFailureCodeFor(taskResult.Status)
                    };
                }
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, "Unexpected exception during task execution");
                taskResult.LogOutput = LHMappingHelper.ExceptionToVariableValue(ex, workerContext);
                taskResult.Status = TaskStatus.TaskFailed;
                taskResult.Error = new LHTaskError
                {
                    Message = ex.ToString(), Type = LHMappingHelper.GetFailureCodeFor(taskResult.Status)
                };
            }

            taskResult.Time = Timestamp.FromDateTime(DateTime.UtcNow);

            return taskResult;
        }

        private async Task<object?> Invoke(ScheduledTask scheduledTask, LHWorkerContext workerContext)
        {
            var inputs = _task.TaskMethodMappings.Select(mapping => mapping.Assign(scheduledTask, workerContext)).ToArray();
            var result = _task.TaskMethod!.Invoke(_task.Executable, inputs);

            if (result is not Task task)
                throw new InvalidConstraintException("Task method must return Task<type> or Task");

            // DO NOT MOVE THIS LINE AS WE NEED THE TASK TO BE AWAITED BEFORE RETURNING THE RESULT
            await task;

            return !_task.TaskMethod.ReturnType.IsGenericType ? null : ((dynamic)task).Result;
        }
    }
}
