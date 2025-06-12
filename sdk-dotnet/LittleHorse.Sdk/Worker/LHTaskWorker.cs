using Grpc.Core;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Worker.Internal;
using Microsoft.Extensions.Logging;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk.Worker
{
    /// <summary>
    /// The LHTaskWorker talks to the LH Servers and executes a specified Task Method every time a Task
    /// is scheduled.
    /// </summary>
    /// <typeparam name="T">
    /// The type of the executable object.
    /// </typeparam>
    public class LHTaskWorker<T>
    {
        private readonly LHConfig _config;
        private readonly ILogger<LHTaskWorker<T>>? _logger;
        private LHServerConnectionManager<T>? _manager;
        private readonly LittleHorseClient _lhClient;
        private readonly LHTask<T> _task;
        private readonly CancellationTokenSource _cancellationTokenSource;

        /// <value>Property <c>TaskDefName</c> represents the name of the TaskDef.</value>
        public string TaskDefName => _task.TaskDefName;

        /// <summary>
        /// Constructor of the LHTaskWorker.
        /// </summary>
        /// <param name="executable">Custom class where customer business logic will be added.</param>
        /// <param name="taskDefName">The name of the task.</param>
        /// <param name="config">The LH configuration object.</param>
        /// <param name="cancellationToken"></param>
        public LHTaskWorker(T executable, string taskDefName, LHConfig config, CancellationToken cancellationToken = default)
        {
            _config = config;
            _logger = LHLoggerFactoryProvider.GetLogger<LHTaskWorker<T>>();
            _lhClient = _config.GetGrpcClientInstance();
            _task = new LHTask<T>(executable, taskDefName, _lhClient);
            _cancellationTokenSource = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
        }

        /// <summary>
        /// Starts polling for and executing tasks.
        /// </summary>
        /// <exception cref="LHMisconfigurationException">
        /// if the schema from the TaskDef configured in the configProps is
        /// incompatible with the method signature from the provided executable Dotnet object, or if
        /// the Worker cannot connect to the LH Server.
        /// </exception>
        public async Task Start()
        {
            if (!await TaskDefExists())
            {
                throw new LHMisconfigurationException($"Couldn't find TaskDef: {_task.TaskDefName}");
            }

            await _task.PrepareLHTaskMethod();
            if (_manager == null)
            {
                _manager = new LHServerConnectionManager<T>(_config, _task, _lhClient, _cancellationTokenSource.Token);
                await _manager.Start();
            }
        }

        /// <summary>
        /// Checks if the TaskDef exists
        /// </summary>
        /// <returns>
        /// true if the task is registered or false otherwise
        /// </returns>
        /// <exception cref="RpcException"> Throws when call fails.
        /// </exception>
        public async Task<bool> TaskDefExists()
        {
            try
            {
                await _task.GetTaskDef();

                return true;
            }
            catch (RpcException ex)
            {
                if (ex.StatusCode == StatusCode.NotFound)
                {
                    return false;
                }

                throw;
            }
        }

        /// <summary>
        /// Cleanly shuts down the Task Worker.
        /// </summary>
        public void Close()
        {
            _cancellationTokenSource.Cancel();
            _cancellationTokenSource.Dispose();
        }

        /// <summary>
        /// Deploys the TaskDef object to the LH Server. This is a convenience method, generally not
        /// recommended for production(in production you should manually use the PutTaskDef).
        /// </summary>
        public async Task RegisterTaskDef()
        {
            await RegisterTaskDef(false);
        }

        /// <summary>
        /// Deploys the TaskDef object to the LH Server. This is a convenience method, generally not
        /// recommended for production(in production you should manually use the PutTaskDef).
        /// </summary>
        /// <param name="swallowAlreadyExists">
        /// If true, then ignore grpc ALREADY_EXISTS error when registering the TaskDef.
        /// </param>
        private async Task RegisterTaskDef(bool swallowAlreadyExists)
        {
            _logger?.LogInformation($"Creating TaskDef: {_task.TaskDefName}");

            try
            {
                var signature = new LHTaskSignature<T>(_task.TaskDefName, _task.Executable);

                var request = new PutTaskDefRequest
                {
                    Name = _task.TaskDefName
                };

                foreach (var lhMethodParam in signature.LhMethodParams)
                {
                    var variableDef = new VariableDef
                    {
                        Name = lhMethodParam.Name,
                        Type = lhMethodParam.Type,
                        MaskedValue = lhMethodParam.IsMasked
                    };

                    request.InputVars.Add(variableDef);
                }

                if (signature.ReturnType != null) {
                    request.ReturnType = signature.ReturnType;
                }

                var response = await _lhClient.PutTaskDefAsync(request);

                _logger?.LogInformation("Created TaskDef:\n{}", LHMappingHelper.ProtoToJson(response));
            }
            catch (RpcException ex)
            {
                if (swallowAlreadyExists && ex.StatusCode == StatusCode.AlreadyExists)
                {
                    _logger?.LogInformation("TaskDef {} already exists!", _task.TaskDefName);
                }
                else
                {
                    throw;
                }
            }
        }
    }
}
