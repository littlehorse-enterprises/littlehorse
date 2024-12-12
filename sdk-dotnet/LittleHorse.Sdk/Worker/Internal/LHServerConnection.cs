using Grpc.Core;
using LittleHorse.Common.Proto;
using LittleHorse.Sdk.Helper;
using Microsoft.Extensions.Logging;
using static LittleHorse.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk.Worker.Internal
{
    public class LHServerConnection<T> : IDisposable
    {
        private readonly LHServerConnectionManager<T> _connectionManager;
        private readonly LHHostInfo _hostInfo;
        private bool _running;
        private readonly LittleHorseClient _client;
        private AsyncDuplexStreamingCall<PollTaskRequest, PollTaskResponse> _call;
        private readonly ILogger? _logger;

        public LHHostInfo HostInfo { get { return _hostInfo; } }

        public LHServerConnection(LHServerConnectionManager<T> connectionManager, LHHostInfo hostInfo)
        {
            _connectionManager = connectionManager;
            _hostInfo = hostInfo;
            _logger = LHLoggerFactoryProvider.GetLogger<LHServerConnection<T>>();
            _client = _connectionManager.Config.GetGrpcClientInstance(hostInfo.Host, hostInfo.Port);
            _call = _client.PollTask();
        }

        public void Connect()
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
                         var wFRunId = LHHelper.GetWfRunId(scheduledTask.Source);
                         _logger?.LogDebug($"Received task schedule request for wfRun {wFRunId?.Id}");

                         _connectionManager.SubmitTaskForExecution(scheduledTask, _client);

                         _logger?.LogDebug($"Scheduled task on threadpool for wfRun {wFRunId?.Id}");
                     }
                     else
                     {
                         _logger?.LogError("Didn't successfully claim task, likely due to server restart.");
                     }

                     if (_running)
                     {
                         await _call.RequestStream.WriteAsync(request);
                         _logger?.LogDebug($"Request work on {_hostInfo.Host} : {_hostInfo.Port}");
                     }
                     else
                     {
                         await _call.RequestStream.CompleteAsync();
                     }
                 }
             });

            await _call.RequestStream.WriteAsync(request);
            _logger?.LogDebug($"Request work on {_hostInfo.Host} : {_hostInfo.Port}");
            await readTask;
        }

        public void Dispose()
        {
            _running = false;
        }

        public bool IsSame(LHHostInfo hostInfoToCompare)
        {
            return _hostInfo.Host.Equals(hostInfoToCompare.Host) && _hostInfo.Port == hostInfoToCompare.Port;
        }
    }
}
