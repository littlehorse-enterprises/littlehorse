using Google.Protobuf.Collections;
using Google.Protobuf.WellKnownTypes;
using LittleHorse.Common.Configuration;
using LittleHorse.Common.Exceptions;
using LittleHorse.Worker.Internal.Helpers;
using LittleHorse.Common.Proto;
using Microsoft.Extensions.Logging;
using Polly;
using System.Reflection;

namespace LittleHorse.Worker.Internal
{
    public class LHServerConnectionManager<T> : IDisposable
    {
        private const int BALANCER_SLEEP_TIME = 5000;
        private const int MAX_REPORT_RETRIES = 5;

        private ILHWorkerConfig _config;
        private MethodInfo _taskMethod;
        private TaskDef _taskDef;
        private List<VariableMapping> _mappings;
        private T _executable;
        private ILogger? _logger;
        private LHPublicApi.LHPublicApiClient _bootstrapClient;
        private bool _running;
        private List<LHServerConnection<T>> _runningConnections;
        private Thread _rebalanceThread;
        private SemaphoreSlim _semaphore;

        public ILHWorkerConfig Config { get { return _config; } }
        public TaskDef TaskDef { get { return _taskDef; } }

        public LHServerConnectionManager(ILHWorkerConfig config,
                                         MethodInfo taskMethod,
                                         TaskDef taskDef,
                                         List<VariableMapping> mappings,
                                         T executable,
                                         ILogger? logger = null)
        {
            _config = config;
            _taskMethod = taskMethod;
            _taskDef = taskDef;
            _mappings = mappings;
            _executable = executable;
            _logger = logger;

            _bootstrapClient = config.GetGrcpClientInstance();

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
                var request = new RegisterTaskWorkerRequest()
                {
                    TaskDefName = _taskDef.Name,
                    ClientId = _config.ClientId,
                    ListenerName = _config.ConnectListener
                };

                var response = _bootstrapClient.RegisterTaskWorker(request);

                HandleRegisterTaskWorkResponse(response);

            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, $"Failed contacting bootstrap host {_config.BootstrapHost}:{_config.BootstrapPort}");
            }
        }

        private void HandleRegisterTaskWorkResponse(RegisterTaskWorkerResponse response)
        {

            response.YourHosts.ToList().ForEach(async host =>
            {
                if (!IsAlreadyRunning(host))
                {
                    try
                    {
                        var newConnection = new LHServerConnection<T>(this, host);
                        await newConnection.Connect();
                        _runningConnections.Add(newConnection);
                        _logger?.LogInformation($"Adding connection to: {host.Host} : {host.Port} for {_taskDef.Name}");
                    }
                    catch (IOException ex)
                    {
                        _logger?.LogError(ex, $"Exception on HandleRegisterTaskWorkResponse.");
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
            return hosts.ToList().Any(host => runningThread.IsSame(host));
        }

        private bool IsAlreadyRunning(LHHostInfo host)
        {
            return _runningConnections.Any(conn => conn.IsSame(host));
        }

        public async void SubmitTaskForExecution(ScheduledTask scheduledTask, LHPublicApi.LHPublicApiClient client)
        {
            await _semaphore.WaitAsync();

            DoTask(scheduledTask, client);
        }

        private void DoTask(ScheduledTask scheduledTask, LHPublicApi.LHPublicApiClient client)
        {
            ReportTaskRun result = ExecuteTask(scheduledTask, LHMappingHelper.MapDateTimeFromProtoTimeStamp(scheduledTask.CreatedAt));
            _semaphore.Release();

            var wfRunId = LHWorkerHelper.GetWFRunId(scheduledTask.Source);

            try
            {
                var retriesLeft = MAX_REPORT_RETRIES;

                _logger?.LogDebug($"Going to report task for wfRun {wfRunId}");
                Policy.Handle<Exception>().WaitAndRetry(MAX_REPORT_RETRIES,
                    retryAttempt => TimeSpan.FromSeconds(5),
                    onRetry: (exception, timeSpan, retryCount, context) =>
                {
                    --retriesLeft;
                    _logger?.LogDebug($"Failed to report task for wfRun {wfRunId}: {exception.Message}. Retries left: {retriesLeft}");
                    _logger?.LogDebug($"Retrying reportTask rpc on taskRun {LHWorkerHelper.TaskRunIdToString(result.TaskRunId)}");
                }).Execute(() => RunReportTask(result));
            }
            catch (Exception ex)
            {
                _logger?.LogDebug($"Failed to report task for wfRun {wfRunId}: {ex.Message}. No retries left.");
            }
        }

        private void RunReportTask(ReportTaskRun reportedTask)
        {
            var response = _bootstrapClient.ReportTask(reportedTask);
        }

        private ReportTaskRun ExecuteTask(ScheduledTask scheduledTask, DateTime? scheduleTime)
        {
            var taskResult = new ReportTaskRun()
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
                taskResult.Status = LittleHorse.Common.Proto.TaskStatus.TaskSuccess;

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
                taskResult.Status = LittleHorse.Common.Proto.TaskStatus.TaskInputVarSubError;
            }
            catch (LHSerdeException ex)
            {
                _logger?.LogError(ex, "Failed serializing Task Output");
                taskResult.LogOutput = LHMappingHelper.MapExceptionToVariableValue(ex, workerContext);
                taskResult.Status = LittleHorse.Common.Proto.TaskStatus.TaskOutputSerializingError;
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, "Unexpected exception during task execution");
                taskResult.LogOutput = LHMappingHelper.MapExceptionToVariableValue(ex, workerContext);
                taskResult.Status = LittleHorse.Common.Proto.TaskStatus.TaskFailed;
            }

            taskResult.Time = Timestamp.FromDateTime(DateTime.UtcNow);

            return taskResult;
        }

        private object? Invoke(ScheduledTask scheduledTask, LHWorkerContext workerContext)
        {
            var inputs = _mappings.Select(mapping => mapping.Assign(scheduledTask, workerContext)).ToArray();

            return _taskMethod.Invoke(_executable, inputs);
        }

        public void CloseConnection(LHServerConnection<T> connection)
        {
            var currConn = _runningConnections.Where(c => c.IsSame(connection.HostInfo)).FirstOrDefault();

            if (currConn != null)
            {
                _runningConnections.Remove(currConn);
            }
        }
    }
}
