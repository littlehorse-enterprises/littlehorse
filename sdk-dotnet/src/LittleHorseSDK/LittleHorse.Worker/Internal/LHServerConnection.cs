using Grpc.Core;
using Grpc.Core.Utils;
using LittleHorse.Worker.Internal.Helpers;
using LittleHorseSDK.Common.proto;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Worker.Internal
{
    public class LHServerConnection<T> : IDisposable
    {
        private LHServerConnectionManager<T> _connectionManager;
        private HostInfo _hostInfo;
        private bool _running;
        private LHPublicApi.LHPublicApiClient _client;
        private AsyncDuplexStreamingCall<PollTaskRequest, PollTaskResponse> _call;
        private ILogger? _logger;

        public HostInfo HostInfo { get { return _hostInfo; } }

        public LHServerConnection(LHServerConnectionManager<T> connectionManager, HostInfo hostInfo, ILogger? logger = null) 
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
            try
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
                    if(taskToDo.Result != null)
                    {
                        var scheduledTask = taskToDo.Result;
                        var wFRunId = LHWorkerHelper.GetWFRunId(scheduledTask.Source);

                        _logger?.LogInformation($"Received task schedule request for wfRun {wFRunId}");

                        _connectionManager.SubmitTaskForExecution(scheduledTask, _client);
                    } 
                    else
                    {
                        _logger?.LogError($"Didn't successfully claim task: {taskToDo.Code.ToString()} {taskToDo.Message}");
                    }

                    if(_running)
                    {
                        await RequestMoreWorkAsync();
                    }
                    else
                    {
                        await _call.RequestStream.CompleteAsync();
                        _connectionManager.CloseConnection(this);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, "Unexpected error from server");
                _running = false;
                _connectionManager.CloseConnection(this);
            }
        }

        public void Dispose()
        {
            _running = false;
        }

        public bool IsSame(HostInfo hostInfoToCompare)
        {
            return _hostInfo.Host.Equals(hostInfoToCompare.Host) && _hostInfo.Port == hostInfoToCompare.Port;
        }
    }
}
