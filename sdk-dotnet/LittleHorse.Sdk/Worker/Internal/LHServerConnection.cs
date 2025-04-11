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
    public class LHServerConnection<T> : IDisposable
    {
        private const int REPORT_TASK_RETRIES_INTERVAL_SECONDS = 2;
        private const int MAX_REPORT_RETRIES = 15;
        private const int POLLTASK_SLEEP_TIME = 5000;
        
        private readonly LHServerConnectionManager<T> _connectionManager;
        private readonly LHHostInfo _hostInfo;
        private bool _running;
        private readonly LittleHorseClient _client;
        private readonly AsyncDuplexStreamingCall<PollTaskRequest, PollTaskResponse> _call;
        private readonly ILogger? _logger;
        private SemaphoreSlim _reportTaskSemaphore;
        private readonly LHTask<T> _task;

        public LHHostInfo HostInfo => _hostInfo;

        public LHServerConnection(LHServerConnectionManager<T> connectionManager, LHHostInfo hostInfo, LHTask<T> task)
        {
            _connectionManager = connectionManager;
            _hostInfo = hostInfo;
            _logger = LHLoggerFactoryProvider.GetLogger<LHServerConnection<T>>();
            _client = _connectionManager.Config.GetGrpcClientInstance(hostInfo.Host, hostInfo.Port);
            _call = _client.PollTask();
            _reportTaskSemaphore = new SemaphoreSlim(_connectionManager.Config.WorkerThreads);
            _task = task;
        }

        public void Start()
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
                         Thread.Sleep(POLLTASK_SLEEP_TIME);
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

        public void Dispose()
        {
            _running = false;
            _call.Dispose();
            _reportTaskSemaphore.Dispose();
            _reportTaskSemaphore = new SemaphoreSlim(_connectionManager.Config.WorkerThreads);
        }

        public bool IsSame(string host, int port)
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
            const int maxRetries = MAX_REPORT_RETRIES;
            int retriesLeft = maxRetries;

            _logger?.LogDebug($"Starting task reporting for wfRun {wfRunId?.Id} and " +
                              $"TaskRunId {result.TaskRunId.TaskGuid}.");
            
            var retryPolicy = Policy
                .Handle<Exception>()
                .WaitAndRetry(
                    retryCount: maxRetries,
                    sleepDurationProvider: retryAttempt => TimeSpan.FromSeconds(REPORT_TASK_RETRIES_INTERVAL_SECONDS),
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
