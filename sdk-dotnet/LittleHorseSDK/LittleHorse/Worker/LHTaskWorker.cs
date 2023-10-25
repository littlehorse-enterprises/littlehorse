using Grpc.Core;
using LittleHorse.Common.Configuration;
using LittleHorse.Common.Exceptions;
using LittleHorse.Worker.Internal;
using LittleHorse.Worker.Internal.Helpers;
using LittleHorse.Common.Proto;
using Microsoft.Extensions.Logging;
using System.Reflection;

namespace LittleHorse.Worker
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
        private ILHWorkerConfig _config;
        private ILogger<LHTaskWorker<T>>? _logger;
        private T _executable;
        private TaskDef? _taskDef;
        private MethodInfo? _taskMethod;
        private List<VariableMapping> _mappings;
        private LHTaskSignature<T>? _taskSignature;
        private LHServerConnectionManager<T>? _manager;
        private string _taskDefName;
        private LHPublicApi.LHPublicApiClient _grpcClient;

        public string TaskDefName { get => _taskDefName; }

        public LHTaskWorker(T executable, string taskDefName, ILHWorkerConfig config, ILogger<LHTaskWorker<T>>? logger = null)
        {
            _config = config;
            _logger = logger;
            _executable = executable;
            _mappings = new List<VariableMapping>();
            _taskDefName = taskDefName;
            _grpcClient = _config.GetGrcpClientInstance();
        }

        /// <summary>
        /// Starts polling for and executing tasks.
        /// </summary>
        /// <exception cref="LHMisconfigurationException">
        /// if the schema from the TaskDef configured in the configProps is
        /// incompatible with the method signature from the provided executable Java object, or if
        /// the Worker cannot connect to the LH Server.
        /// </exception>
        public void Start()
        {
            if (!TaskDefExists())
            {
                throw new LHMisconfigurationException($"Couldn't find TaskDef: {_taskDefName}");
            }

            _taskSignature = new LHTaskSignature<T>(_taskDefName, _executable);
            _taskMethod = _taskSignature.TaskMethod;

            ValidateTaskMethodParameters(_taskMethod, _taskSignature);
            _mappings = CreateVariableMappings(_taskMethod, _taskSignature);

            _manager = new LHServerConnectionManager<T>(_config, _taskMethod, GetTaskDef(), _mappings, _executable, _logger);

            _manager.Start();
        }

        /// <summary>
        /// Checks if the TaskDef exists
        /// </summary>
        /// <returns>
        /// true if the task is registered or false otherwise
        /// </returns>
        /// <exception cref="RpcException"> Throws when call fails.
        /// </exception>
        public bool TaskDefExists()
        {
            try
            {
                var taskDefId = new TaskDefId()
                {
                    Name = _taskDefName,
                };
                _grpcClient.GetTaskDef(taskDefId);

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
            _manager?.Dispose();
        }

        /// <summary>
        /// Deploys the TaskDef object to the LH Server. This is a convenience method, generally not
        /// recommended for production(in production you should manually use the PutTaskDef).
        /// </summary>
        public void RegisterTaskDef()
        {
            RegisterTaskDef(false);
        }

        /// <summary>
        /// Deploys the TaskDef object to the LH Server. This is a convenience method, generally not
        /// recommended for production(in production you should manually use the PutTaskDef).
        /// </summary>
        /// <param name="swallowAlreadyExists">
        /// If true, then ignore grpc ALREADY_EXISTS error when registering the TaskDef.
        /// </param>
        public void RegisterTaskDef(bool swallowAlreadyExists)
        {
            _logger?.LogInformation($"Creating TaskDef: {_taskDefName}");

            try
            {
                var signature = new LHTaskSignature<T>(_taskDefName, _executable);

                var request = new PutTaskDefRequest()
                {
                    Name = _taskDefName
                };

                for (int i = 0; i < signature.VarNames.Count; i++)
                {
                    request.InputVars.Add(new VariableDef()
                    {
                        Name = signature.VarNames[i],
                        Type = signature.ParamTypes[i]
                    });
                }

                var response = _grpcClient.PutTaskDef(request);
                _logger?.LogInformation($"Created TaskDef:\n{LHMappingHelper.MapProtoToJson(response)}");
            }
            catch (RpcException ex)
            {
                if (swallowAlreadyExists && ex.StatusCode == StatusCode.AlreadyExists)
                {
                    _logger?.LogInformation($"TaskDef {_taskDefName} already exists!");
                }
                else
                {
                    throw;
                }
            }
        }

        private TaskDef GetTaskDef()
        {
            if (_taskDef is null)
            {
                _taskDef = _config.GetTaskDef(_taskDefName);
            }

            return _taskDef;
        }

        private void ValidateTaskMethodParameters(MethodInfo taskMethod, LHTaskSignature<T> taskSignature)
        {

            if (taskSignature.HasWorkerContextAtEnd)
            {
                if (taskSignature.TaskMethod.GetParameters().Length - 1 != GetTaskDef().InputVars.Count)
                {
                    throw new LHTaskSchemaMismatchException("Number of task method params doesn't match number of taskdef params!");
                }
            }
            else
            {
                if (taskMethod.GetParameters().Length != GetTaskDef().InputVars.Count)
                {
                    throw new LHTaskSchemaMismatchException("Number of task method params doesn't match number of taskdef params!");
                }
            }
        }

        private List<VariableMapping> CreateVariableMappings(MethodInfo taskMethod, LHTaskSignature<T> taskSignature)
        {
            var mappings = new List<VariableMapping>();

            var taskParams = taskMethod.GetParameters();

            for (int index = 0; index < GetTaskDef().InputVars.Count; index++)
            {
                var taskParam = taskParams[index];

                if (taskParam.ParameterType.IsAssignableFrom(typeof(LHWorkerContext)))
                {
                    throw new LHTaskSchemaMismatchException("Can only have WorkerContext after all required taskDef params.");
                }

                mappings.Add(CreateVariableMapping(GetTaskDef(), index, taskParam.ParameterType, taskParam.Name));
            }

            if (taskSignature.HasWorkerContextAtEnd)
            {
                mappings.Add(CreateVariableMapping(GetTaskDef(), taskParams.Count() - 1, typeof(LHWorkerContext), null));
            }

            return mappings;
        }

        private VariableMapping CreateVariableMapping(TaskDef taskDef, int index, Type type, string? paramName)
        {
            return new VariableMapping(taskDef, index, type, paramName);
        }

    }
}
