using LittleHorse.Common.Configuration;
using LittleHorse.Common.Exceptions;
using LittleHorse.Worker.Internal;
using LittleHorseSDK.Common.proto;
using Microsoft.Extensions.Logging;
using System.Reflection;

namespace LittleHorse.Worker
{
    public class LHTaskWorker<T>
    {
        private ILHWorkerConfig _config;
        private ILogger? _logger;
        private T _executable;
        private TaskDef? _taskDef;
        private MethodInfo _taskMethod;
        private List<VariableMapping> _mappings;
        private LHTaskSignature<T> _taskSignature;
        private LHServerConnectionManager<T> _manager;
        private string _taskDefName;

        public string TaskDefName { get => _taskDefName; }

        public LHTaskWorker(ILHWorkerConfig config, string taskDefName, T executable, ILogger? logger = null) 
        { 
            try
            {
                _config = config;
                _logger = logger;
                _executable = executable;
                _mappings = new List<VariableMapping>();
                _taskDefName = taskDefName;

                if (GetTaskDef() is null)
                {
                    var errorMessage = $"Couldn't find TaskDef: {TaskDefName}";
                    throw new LHApiException(errorMessage, LHResponseCode.NotFoundError);
                }

                _taskSignature = new LHTaskSignature<T>(_taskDefName, _executable);

                _taskMethod = _taskSignature.TaskMethod;

                ValidateTaskMethodParameters();
                CreateVariableMappings();

                _manager = new LHServerConnectionManager<T>(config, _taskMethod, GetTaskDef(), _mappings, _executable, _logger);

            }
            catch(LHTaskSchemaMismatchException ex)
            {
                throw new LHApiException("Provided C# method does not match registered task!", LHResponseCode.BadRequestError, ex);
            }
            catch(IOException ex)
            {
                throw new LHApiException("Couldn't create connection to LH", ex);
            }
            
        }

        public void Start()
        {
            _manager.Start();
        }

        public void Close()
        {
            _manager.Dispose();
        }

        private TaskDef GetTaskDef()
        {
            if(_taskDef is null)
            {
                _taskDef = _config.GetTaskDef(_taskDefName);
            }

            return _taskDef;
        }

        private void ValidateTaskMethodParameters()
        {
            if (_taskSignature.HasWorkerContextAtEnd)
            {
                if (_taskSignature.TaskMethod.GetParameters().Length - 1 != GetTaskDef().InputVars.Count)
                {
                    throw new LHTaskSchemaMismatchException("Number of task method params doesn't match number of taskdef params!");
                }
            }
            else
            {
                if (_taskMethod.GetParameters().Length != GetTaskDef().InputVars.Count)
                {
                    throw new LHTaskSchemaMismatchException("Number of task method params doesn't match number of taskdef params!");
                }
            }
        }

        private List<VariableMapping> CreateVariableMappings()
        {
            var mappings = new List<VariableMapping>();

            var taskParams = _taskMethod.GetParameters();

            for (int index = 0; index < GetTaskDef().InputVars.Count; index++)
            {
                var taskParam = taskParams[index];

                if (taskParam.GetType().IsAssignableFrom(typeof(LHWorkerContext)))
                {
                    throw new LHTaskSchemaMismatchException("Can only have WorkerContext after all required taskDef params.");
                }

                mappings.Add(CreateVariableMapping(GetTaskDef(), index, taskParam.GetType(), taskParam.Name));
            }

            if (_taskSignature.HasWorkerContextAtEnd)
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
