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
    internal class LHServerConnectionManager<T> : IDisposable
    {
        private const int BalancerSleepTime = 5000;
        private const int GrpcUnaryCallTimeoutSeconds = 30;

        private readonly LHConfig _config;
        private readonly ILogger? _logger;
        private readonly LittleHorseClient _bootstrapClient;
        private bool _running;
        private readonly List<LHServerConnection<T>> _runningConnections;
        private readonly Thread _rebalanceThread;
        private readonly LHTask<T> _task;
        
        internal LHConfig Config => _config;
        internal TaskDef TaskDef => _task.TaskDef!;

        internal LHServerConnectionManager(LHConfig config,
                                         LHTask<T> task, LittleHorseClient bootstrapClient)
        {
            _config = config;
            _logger = LHLoggerFactoryProvider.GetLogger<LHServerConnectionManager<T>>();
            _task = task;
            _bootstrapClient = bootstrapClient;
            _running = false;
            _runningConnections = new List<LHServerConnection<T>>();
            _rebalanceThread = new Thread(RebalanceWork);
        }

        /// <summary>
        /// Starts the connection manager.
        /// </summary>
        internal void Start()
        {
            _running = true;
            _rebalanceThread.Start();
        }

        /// <summary>
        /// Stops the connection manager and all connections.
        /// </summary>
        public void Dispose()
        {
            _running = false;
        }

        private void RebalanceWork()
        {
            while (_running)
            {
                DoHeartBeat();
                Thread.Sleep(BalancerSleepTime);
            }
        }

        private void DoHeartBeat()
        {
            try
            {
                var request = new RegisterTaskWorkerRequest
                {
                    TaskDefId = _task.TaskDef!.Id,
                    TaskWorkerId = _config.WorkerId
                };
                var response = _bootstrapClient.RegisterTaskWorker(request: request,
                    deadline: DateTime.UtcNow.AddSeconds(GrpcUnaryCallTimeoutSeconds));
                
                HandleRegisterTaskWorkerResponse(response);
            }
            catch (Exception ex)
            {
                switch (ex.InnerException)
                {
                    case RpcException { StatusCode: StatusCode.Internal }:
                        _logger?.LogError(ex,
                            $"Failed contacting bootstrap host {_config.BootstrapHost}:{_config.BootstrapPort}");
                        break;
                    case RpcException { StatusCode: StatusCode.DeadlineExceeded }:
                        _logger?.LogError(ex, "Deadline exceeded trying to register task worker.");
                        break;
                    default:
                        _logger?.LogError(ex, "Something happened trying to contact the bootstrap server.");
                        break;
                }

                CloseAllConnections();
                Thread.Sleep(BalancerSleepTime);
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
                        var newConnection = new LHServerConnection<T>(this, host, _task);
                        newConnection.Start();
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
        
        private void CloseAllConnections()
        {
            _runningConnections.RemoveAll(serverConnection =>
            {
                serverConnection.Dispose();
            
                return true;
            });
        }
    }
}
