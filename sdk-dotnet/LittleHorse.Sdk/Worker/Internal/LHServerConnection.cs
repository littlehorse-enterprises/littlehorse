using System.Reflection;
using Google.Protobuf.WellKnownTypes;
using Grpc.Core;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using Microsoft.Extensions.Logging;
using Polly;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;
using LHTaskException = LittleHorse.Sdk.Exceptions.LHTaskException;
using TaskStatus = LittleHorse.Sdk.Common.Proto.TaskStatus;

namespace LittleHorse.Sdk.Worker.Internal
{
    /// <summary>
    /// Represents a connection to the LH server for a specific task type.
    /// </summary>
    /// <typeparam name="T">The type of the task.</typeparam>
    internal class LHServerConnection<T> : IDisposable
    {
        private const int ReportTaskRetriesIntervalSeconds = 2;
        private const int MaxReportRetries = 15;
        private const int PolltaskSleepTime = 5000;
        
        private readonly LHServerConnectionManager<T> _connectionManager;
        private readonly LHHostInfo _hostInfo;
        private bool _running;
        private readonly LittleHorseClient _client;
        private readonly AsyncDuplexStreamingCall<PollTaskRequest, PollTaskResponse> _call;
        private readonly ILogger? _logger;
        private SemaphoreSlim _reportTaskSemaphore;
        private readonly LHTask<T> _task;

        internal LHHostInfo HostInfo => _hostInfo;

        /// <summary>
        /// Creates a new instance of the <see cref="LHServerConnection{T}"/> class.
        /// </summary>
        /// <param name="connectionManager">Object to handle all available server connections.</param>
        /// <param name="hostInfo">Information of the current host.</param>
        /// <param name="task">Prepares a task method to be executed by the server.</param>
        internal LHServerConnection(LHServerConnectionManager<T> connectionManager, LHHostInfo hostInfo, LHTask<T> task)
        {
            _connectionManager = connectionManager;
            _hostInfo = hostInfo;
            _logger = LHLoggerFactoryProvider.GetLogger<LHServerConnection<T>>();
            _client = _connectionManager.Config.GetGrpcClientInstance(hostInfo.Host, hostInfo.Port);
            _call = _client.PollTask();
            _reportTaskSemaphore = new SemaphoreSlim(_connectionManager.Config.WorkerThreads);
            _task = task;
        }

        /// <summary>
        /// Starts a new async task which will poll the server for tasks to execute.
        /// </summary>
        internal void Start()
        {
            _running = true;
            Task.Run(RequestMoreWorkAsync);
        }

        private async Task RequestMoreWorkAsync()
        {
            var request = new PollTaskRequest
            {
                ClientId = _connectionManager.Config.WorkerId,
                TaskDefId = _connectionManager.TaskDef.Id,
                TaskWorkerVersion = _connectionManager.Config.TaskWorkerVersion
            };

            var readTask = Task.Run(async () =>
             {
                 await foreach (var taskToDo in _call.ResponseStream.ReadAllAsync())
                 {
                     if (taskToDo.Result != null)
                     {
                         var scheduledTask = taskToDo.Result;
                         var wFRunId = LHTaskHelper.GetWfRunId(scheduledTask.Source);
                         _logger?.LogDebug($"Received task schedule request for wfRun {wFRunId?.Id}");

                         await _reportTaskSemaphore.WaitAsync();

                         await DoTask(scheduledTask);

                         _logger?.LogDebug($"Scheduled task on threadpool for wfRun {wFRunId?.Id}");
                     }
                     else
                     {
                         _logger?.LogError("Didn't successfully claim task, likely due to a server crash.");
                         Thread.Sleep(PolltaskSleepTime);
                     }
                     _reportTaskSemaphore.Release();

                     if (_running)
                     {
                         await _call.RequestStream.WriteAsync(request);
                     }
                     else
                     {
                         await _call.RequestStream.CompleteAsync();
                     }
                 }
             });

            await _call.RequestStream.WriteAsync(request);

            await readTask;
        }

        /// <summary>
        /// This method ensures that all resources are properly released when the connection is no longer needed.
        /// </summary>
        public void Dispose()
        {
            _running = false;
            _call.Dispose();
            _reportTaskSemaphore.Dispose();
            _reportTaskSemaphore = new SemaphoreSlim(_connectionManager.Config.WorkerThreads);
        }

        /// <summary>
        /// This method checks if the current connection is the same as the specified host and port.
        /// </summary>
        /// <param name="host">Host to be verified.</param>
        /// <param name="port">Port to be verified.</param>
        /// <returns></returns>
        internal bool IsSame(string host, int port)
        {
            return _hostInfo.Host.Equals(host) && _hostInfo.Port == port;
        }
      
        private async Task DoTask(ScheduledTask scheduledTask)
        {
            ReportTaskRun result = ExecuteTask(scheduledTask, LHMappingHelper.DateTimeFromProtoTimeStamp(scheduledTask.CreatedAt));
            
            var wfRunId = LHTaskHelper.GetWfRunId(scheduledTask.Source);

            try
            {
                await ReportTaskWithRetries(result, wfRunId);
            }
            catch (Exception ex)
            {
                _logger?.LogDebug($"Failed to report task for wfRun {wfRunId}: {ex.Message}. No retries left.");
            }
        }

        private async Task ReportTaskWithRetries(ReportTaskRun result, WfRunId? wfRunId)
        {
            const int maxRetries = MaxReportRetries;
            int retriesLeft = maxRetries;

            _logger?.LogDebug($"Starting task reporting for wfRun {wfRunId?.Id} and " +
                              $"TaskRunId {result.TaskRunId.TaskGuid}.");
            
            var retryPolicy = Policy
                .Handle<Exception>()
                .WaitAndRetry(
                    retryCount: maxRetries,
                    sleepDurationProvider: retryAttempt => TimeSpan.FromSeconds(ReportTaskRetriesIntervalSeconds),
                    onRetry: (exception, timeSpan, retryCount, context) =>
                    {
                        retriesLeft--;
                        _logger?.LogDebug($"Retry attempt {retryCount} failed for wfRun {wfRunId} and " +
                                          $"TaskRunId {result.TaskRunId.TaskGuid}. Exception: " +
                                          $"{exception.Message}. Retries left: {retriesLeft}");
                        _logger?.LogDebug("Retrying reportTask rpc on taskRun " +
                                          $"{result.TaskRunId.WfRunId}/{result.TaskRunId.TaskGuid}");
                    });


            await retryPolicy.Execute(() => RunReportTask(result));
        }
        
        private async Task RunReportTask(ReportTaskRun reportedTask)
        {
            await _client.ReportTaskAsync(reportedTask);
        }

        private ReportTaskRun ExecuteTask(ScheduledTask scheduledTask, DateTime? scheduleTime)
        {
            var taskResult = new ReportTaskRun
            {
                TaskRunId = scheduledTask.TaskRunId,
                AttemptNumber = scheduledTask.AttemptNumber
            };

            var workerContext = new LHWorkerContext(scheduledTask, scheduleTime);

            try
            {
                var result = Invoke(scheduledTask, workerContext);
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

        private object? Invoke(ScheduledTask scheduledTask, LHWorkerContext workerContext)
        {
            var inputs = _task.TaskMethodMappings.Select(mapping => mapping.Assign(scheduledTask, workerContext)).ToArray();

            return _task.TaskMethod!.Invoke(_task.Executable, inputs);
        }
    }
}
