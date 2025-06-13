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
        private const int HeartBeatIntervalMs = 5000;
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
                await Task.Delay(HeartBeatIntervalMs);
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
                AddNewConnections(response.YourHosts);
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, "Error when registering task worker.");
                CancelUnassignedHosts(new List<LHHostInfo>());
            }
        }

        /// <summary>
        /// Removes connections that are no longer running, either because they were signaled a cancellation
        /// or encountered an exception during processing.
        /// </summary>
        private void RemoveDeadConnections()
        {
            foreach (var host in _runningConnections.Keys.ToList())
            {
                var connectionsToKeep = _runningConnections[host].Where(task => task.Result.IsRunning()).ToList();
                _runningConnections[host].Where(task => !task.Result.IsRunning()).ToList().ForEach(connection => connection.Dispose());
                _runningConnections[host] = connectionsToKeep;
            }
        }

        /// <summary>
        /// Makes sure the assigned hosts have at least the number of connections specified on LHW_NUM_WORKER_THREADS
        /// </summary>
        /// <param name="assignedHosts">Hosts assigned to the worker</param>
        private void AddNewConnections(IList<LHHostInfo> assignedHosts)
        {
            foreach (var host in assignedHosts)
            {
                var existingConnections = GetExistingConnections(host);
                var missingConnections = Math.Max(_config.WorkerThreads - existingConnections.Count, 0);

                if (missingConnections <= 0) continue;

                var cancellationTokenSource = GetCancellationTokenSource(host);
                var newConnections = CreateConnections(host, missingConnections, cancellationTokenSource.Token);

                existingConnections.AddRange(newConnections);
                _runningConnections[host] = existingConnections;
                _logger?.LogInformation("Added {} connections for host {}:{}", missingConnections, host.Host, host.Port);
            }
        }

        private List<Task<LHServerConnection<T>>> GetExistingConnections(LHHostInfo host)
        {
            _runningConnections.TryGetValue(host, out var connections);
            connections ??= new List<Task<LHServerConnection<T>>>();
            return connections;
        }

        private CancellationTokenSource GetCancellationTokenSource(LHHostInfo host)
        {
            _cancellationTokenSource.TryGetValue(host, out var cancellationTokenSource);
            cancellationTokenSource ??= CancellationTokenSource.CreateLinkedTokenSource(_cancellationToken);
            _cancellationTokenSource[host] = cancellationTokenSource;
            return cancellationTokenSource;
        }

        /// <summary>
        /// Signals a cancellation for connections that are no longer assigned to the worker. The connection task is not immediately disposed
        /// because it might be in the middle of executing a ScheduledTask, thus a cancellation signal is emitted for the connection task
        /// to gracefully finish whatever it's doing..
        /// </summary>
        /// <param name="assignedHosts">Hosts assigned to the worker</param>
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

        private List<Task<LHServerConnection<T>>> CreateConnections(LHHostInfo host, int numberOfConnections, CancellationToken cancellationToken) =>
            Enumerable.Range(0, numberOfConnections).Select(index => Task.Run(() =>
                {
                    _logger?.LogDebug("Adding connection #{} to: {}:{} for task '{}'", index, host.Host, host.Port, _task.TaskDef!.Id);
                    var client = _config.GetGrpcClientInstance(host.Host, host.Port);
                    var newConnection = new LHServerConnection<T>(_config.WorkerId, _task.TaskDef.Id, _config.TaskWorkerVersion, _task, client, cancellationToken);
                    newConnection.Start();
                    return Task.FromResult(newConnection);
                })
            ).ToList();
    }
}
