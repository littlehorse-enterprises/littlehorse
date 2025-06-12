using Google.Protobuf.Collections;
using Grpc.Core;
using LittleHorse.Sdk.Common.Proto;
using Microsoft.Extensions.Logging;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk.Worker.Internal
{
    /// <summary>
    /// Manages the connections to the LH servers for a specific task worker.
    /// </summary>
    /// <typeparam name="T">It is the custom task worker.</typeparam>
    internal class LHServerConnectionManager<T>
    {
        private const int BalancerSleepTime = 5000;
        private const int GrpcUnaryCallTimeoutSeconds = 30;

        private readonly LHConfig _config;
        private readonly ILogger? _logger;
        private readonly LittleHorseClient _bootstrapClient;
        private readonly Dictionary<LHHostInfo, List<Task<LHServerConnection<T>>>> _runningConnections;
        private readonly Dictionary<LHHostInfo, CancellationTokenSource> _cancellationTokenSource;
        private readonly LHTask<T> _task;
        private readonly CancellationToken _cancellationToken;
        private readonly RegisterTaskWorkerRequest _registerTaskWorkerRequest;

        internal LHServerConnectionManager(LHConfig config,
                                         LHTask<T> task, LittleHorseClient bootstrapClient, CancellationToken cancellationToken = default)
        {
            _config = config;
            _logger = LHLoggerFactoryProvider.GetLogger<LHServerConnectionManager<T>>();
            _task = task;
            _bootstrapClient = bootstrapClient;
            _cancellationToken = cancellationToken;
            _runningConnections = new Dictionary<LHHostInfo, List<Task<LHServerConnection<T>>>>();
            _cancellationTokenSource = new Dictionary<LHHostInfo, CancellationTokenSource>();
            _registerTaskWorkerRequest = new RegisterTaskWorkerRequest
            {
                TaskDefId = _task.TaskDef!.Id,
                TaskWorkerId = _config.WorkerId
            };
        }

        /// <summary>
        /// Starts the connection manager.
        /// </summary>
        internal async Task Start()
        {
            while (!_cancellationToken.IsCancellationRequested)
            {
                await DoHeartBeat();
                await Task.Delay(BalancerSleepTime);
            }
        }

        private async Task DoHeartBeat()
        {
            try
            {
                var response = await _bootstrapClient.RegisterTaskWorkerAsync(request: _registerTaskWorkerRequest,
                    deadline: DateTime.UtcNow.AddSeconds(GrpcUnaryCallTimeoutSeconds));
                
                CancelUnassignedHosts(response.YourHosts);
                RemoveDeadConnections();
                AddNewHosts(response.YourHosts);
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, "Error when registering task worker.");
                CloseAllConnections();
                await Task.Delay(BalancerSleepTime);
            }
        }

        private void RemoveDeadConnections()
        {
            foreach (var host in _runningConnections.Keys.ToList())
            {
                var connectionsToKeep = _runningConnections[host].Where(task => task.Result.IsRunning()).ToList();
                _runningConnections[host].Where(task => !task.Result.IsRunning()).ToList().ForEach(connection => connection.Dispose());
                _runningConnections[host] = connectionsToKeep;
            }
        }

        private void AddNewHosts(IList<LHHostInfo> assignedHosts)
        {
            foreach (var host in HostsToAdd(assignedHosts))
            {
                var cancellationTokenSource = CancellationTokenSource.CreateLinkedTokenSource(_cancellationToken);
                _cancellationTokenSource[host] = cancellationTokenSource;
                _runningConnections[host] = ListOfConnections(host, cancellationTokenSource.Token);
                _logger?.LogInformation("Added {} connections for host {}:{}", _runningConnections[host].Count, host.Host, host.Port);
            }
        }

        private List<LHHostInfo> HostsToAdd(IList<LHHostInfo> hosts) =>
            hosts
                .Where(host => !_runningConnections.ContainsKey(host))
                .ToList();

        private void CancelUnassignedHosts(IList<LHHostInfo> assignedHosts)
        {
            foreach (var host in HostsToRemove(assignedHosts))
            {
                _logger?.LogInformation("Cancelling all connections for host {}:{}", host.Host, host.Port);
                _cancellationTokenSource.Remove(host, out var cancellationTokenSource);
                cancellationTokenSource?.Cancel();
                cancellationTokenSource?.Dispose();
            }
        }

        private List<LHHostInfo> HostsToRemove(IList<LHHostInfo> hosts) =>
            _runningConnections.Keys
                .Where(host => !hosts.Contains(host))
                .ToList();

        private List<Task<LHServerConnection<T>>> ListOfConnections(LHHostInfo host, CancellationToken cancellationToken) =>
            Enumerable.Range(0, _config.WorkerThreads).Select(index => Task.Run(() =>
                {
                    _logger?.LogDebug("Adding connection #{} to: {}:{} for task '{}'", index, host.Host, host.Port, _task.TaskDef!.Id);
                    var client = _config.GetGrpcClientInstance(host.Host, host.Port);
                    var newConnection = new LHServerConnection<T>(_config.WorkerId, _task.TaskDef.Id, _config.TaskWorkerVersion, _task, client, cancellationToken);
                    newConnection.Start();
                    return Task.FromResult(newConnection);
                })
            ).ToList();
        
        private void CloseAllConnections()
        {
            _cancellationTokenSource
                .Values
                .ToList()
                .ForEach(cancellationTokenSource =>
                {
                    cancellationTokenSource.Cancel();
                    cancellationTokenSource.Dispose();
                });
            
            _cancellationTokenSource.Clear();
        }
    }
}
