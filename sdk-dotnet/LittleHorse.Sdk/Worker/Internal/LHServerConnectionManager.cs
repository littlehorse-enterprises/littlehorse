using System.Reflection;
using Google.Protobuf.Collections;
using Google.Protobuf.WellKnownTypes;
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
    public class LHServerConnectionManager<T> : IDisposable
    {
        private const int BALANCER_SLEEP_TIME = 5000;
        private const int MAX_REPORT_RETRIES = 5;

        private readonly LHConfig _config;
        private readonly ILogger? _logger;
        private readonly LittleHorseClient _bootstrapClient;
        private bool _running;
        private List<LHServerConnection<T>> _runningConnections;
        private readonly Thread _rebalanceThread;
        private readonly SemaphoreSlim _semaphore;
        private readonly LHTask<T> _task;

        public LHConfig Config => _config;
        public TaskDef TaskDef => _task.TaskDef!;

        public LHServerConnectionManager(LHConfig config,
                                         LHTask<T> task)
        {
            _config = config;
            _logger = LHLoggerFactoryProvider.GetLogger<LHServerConnectionManager<T>>();
            _task = task;
            _bootstrapClient = config.GetGrpcClientInstance();

            _running = false;
            _runningConnections = new List<LHServerConnection<T>>();

            _semaphore = new SemaphoreSlim(config.WorkerThreads);

            _rebalanceThread = new Thread(RebalanceWork);
        }

        public void Start()
        {
            _running = true;
            _rebalanceThread.Start();
        }

        public void Dispose()
        {
            _running = false;
        }

        private void RebalanceWork()
        {
            while (_running)
            {
                DoHeartBeat();
                try
                {
                    Thread.Sleep(BALANCER_SLEEP_TIME);
                }
                catch { }
            }
        }

        private void DoHeartBeat()
        {
            try
            {
                var request = new RegisterTaskWorkerRequest
                {
                    TaskDefId = _task.TaskDef!.Id,
                    TaskWorkerId = _config.WorkerId,
                };

                var response = _bootstrapClient.RegisterTaskWorker(request);

                HandleRegisterTaskWorkerResponse(response);
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, $"Failed contacting bootstrap host {_config.BootstrapHost}:{_config.BootstrapPort}");
                _runningConnections = new List<LHServerConnection<T>>();
            }
        }

        private void HandleRegisterTaskWorkerResponse(RegisterTaskWorkerResponse response)
        {
            response.YourHosts.ToList().ForEach(host =>
            {
                if (!IsAlreadyRunning(host))
                {
                    try
                    {
                        var newConnection = new LHServerConnection<T>(this, host);
                        newConnection.Open();
                        _runningConnections.Add(newConnection);
                        _logger?.LogInformation($"Adding connection to: {host.Host}:{host.Port} for task '{_task.TaskDef!.Id}'");
                    }
                    catch (IOException ex)
                    {
                        _logger?.LogError(ex, "Exception on HandleRegisterTaskWorkResponse.");
                    }
                }
            });

            var lastIndexOfRunningConnection = _runningConnections.Count() - 1;

            for (int i = lastIndexOfRunningConnection; i >= 0; i--)
            {
                var runningThread = _runningConnections[i];
                
                if (!ShouldBeRunning(runningThread, response.YourHosts))
                {
                    _logger?.LogInformation($"Stopping worker thread for host {runningThread.HostInfo.Host} : {runningThread.HostInfo.Port}");

                    runningThread.Dispose();
                    _runningConnections.RemoveAt(i);
                }
            }
        }

        private bool ShouldBeRunning(LHServerConnection<T> runningThread, RepeatedField<LHHostInfo> hosts)
        {
            return hosts.ToList().Any(host => runningThread.IsSame(host.Host, host.Port));
        }

        private bool IsAlreadyRunning(LHHostInfo host)
        {
            return _runningConnections.Any(conn => conn.IsSame(host.Host, host.Port));
        }

        public async void SubmitTaskForExecution(ScheduledTask scheduledTask)
        {
            await _semaphore.WaitAsync();

            DoTask(scheduledTask);
        }

        private void DoTask(ScheduledTask scheduledTask)
        {
            ReportTaskRun result = ExecuteTask(scheduledTask, LHMappingHelper.MapDateTimeFromProtoTimeStamp(scheduledTask.CreatedAt));

            var wfRunId = LHHelper.GetWfRunId(scheduledTask.Source);

            try
            {
                var retriesLeft = MAX_REPORT_RETRIES;

                _logger?.LogDebug($"Going to report task for wfRun {wfRunId?.Id}");
                Policy.Handle<Exception>().WaitAndRetry(MAX_REPORT_RETRIES,
                    retryAttempt => TimeSpan.FromSeconds(5),
                    onRetry: (exception, timeSpan, retryCount, context) =>
                    {
                        --retriesLeft;
                        _logger?.LogDebug(
                            $"Failed to report task for wfRun {wfRunId}: {exception.Message}. Retries left: {retriesLeft}");
                        _logger?.LogDebug(
                            $"Retrying reportTask rpc on taskRun {LHHelper.TaskRunIdToString(result.TaskRunId)}");
                    }).Execute(() => RunReportTask(result));
            }
            catch (Exception ex)
            {
                _logger?.LogDebug($"Failed to report task for wfRun {wfRunId}: {ex.Message}. No retries left.");
            }
            finally
            {
                _semaphore.Release();
            }
        }

        private void RunReportTask(ReportTaskRun reportedTask)
        {
            _bootstrapClient.ReportTask(reportedTask);
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
                var serialized = LHMappingHelper.MapObjectToVariableValue(result);

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
                taskResult.LogOutput = LHMappingHelper.MapExceptionToVariableValue(ex, workerContext);
                taskResult.Status = TaskStatus.TaskInputVarSubError;
                taskResult.Error = new LHTaskError
                {
                    Message = ex.ToString(), Type = LHMappingHelper.GetFailureCodeFor(taskResult.Status)
                };
            }
            catch (LHSerdeException ex)
            {
                _logger?.LogError(ex, "Failed serializing Task Output");
                taskResult.LogOutput = LHMappingHelper.MapExceptionToVariableValue(ex, workerContext);
                taskResult.Status = TaskStatus.TaskOutputSerializingError;
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
                    taskResult.LogOutput = LHMappingHelper.MapExceptionToVariableValue(ex, workerContext);
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
                    taskResult.LogOutput = LHMappingHelper.MapExceptionToVariableValue(ex, workerContext);
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
                taskResult.LogOutput = LHMappingHelper.MapExceptionToVariableValue(ex, workerContext);
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

        public void CloseConnection(string host, int port)
        {
            var currConn = _runningConnections.FirstOrDefault(c => 
                c.IsSame(host, port));

            if (currConn != null)
            {
                _runningConnections.Remove(currConn);
            }
        }
    }
}
