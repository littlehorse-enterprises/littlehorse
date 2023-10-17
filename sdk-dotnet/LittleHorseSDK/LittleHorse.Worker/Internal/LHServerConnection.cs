using Grpc.Core;
using LittleHorse.Worker.Internal.Helpers;
using LittleHorseSDK.Common.proto;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Worker.Internal
{
    public class LHServerConnection<T> : IDisposable
    {
        private LHServerConnectionManager<T> _connectionManager;
        private LHHostInfo _hostInfo;
        private bool _running;
        private LHPublicApi.LHPublicApiClient _client;
        private AsyncDuplexStreamingCall<PollTaskRequest, PollTaskResponse> _call;
        private ILogger? _logger;

        public LHHostInfo HostInfo { get { return _hostInfo; } }

        public LHServerConnection(LHServerConnectionManager<T> connectionManager, LHHostInfo hostInfo, ILogger? logger = null)
        {
            _connectionManager = connectionManager;
            _hostInfo = hostInfo;
            _logger = logger;

            _client = _connectionManager.Config.GetGrcpClientInstance(_hostInfo.Host, _hostInfo.Port);
            _call = _client.PollTask();
        }

        public async Task Connect()
        {
            _running = true;
            await RequestMoreWorkAsync();
        }

        private async Task RequestMoreWorkAsync()
        {

            _logger?.LogDebug($"Request work on {_hostInfo.Host} : {_hostInfo.Port}");

            var request = new PollTaskRequest()
            {
                ClientId = _connectionManager.Config.ClientId,
                TaskDefName = _connectionManager.TaskDef.Name,
                TaskWorkerVersion = _connectionManager.Config.TaskWorkerVersion
            };

            await _call.RequestStream.WriteAsync(request);

            await foreach (var taskToDo in _call.ResponseStream.ReadAllAsync())
            {
                if (taskToDo.Result != null)
                {
                    var scheduledTask = taskToDo.Result;
                    var wFRunId = LHWorkerHelper.GetWFRunId(scheduledTask.Source);

                    _logger?.LogInformation($"Received task schedule request for wfRun {wFRunId}");

                    _connectionManager.SubmitTaskForExecution(scheduledTask, _client);

                    _logger?.LogInformation($"Scheduled task on threadpool for wfRun {wFRunId}");
                }
                else
                {
                    _logger?.LogError($"Didn't successfully claim task, likely due to server restart.");
                }

                if (_running)
                {
                    await RequestMoreWorkAsync();
                }
                else
                {
                    await _call.RequestStream.CompleteAsync();
                }
            }
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
