using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk.Worker.Internal;

internal class LHTask<T>
{
    private TaskDef? _taskDef;
    private MethodInfo? _taskMethod;
    private List<VariableMapping> _taskMethodMappings;
    private readonly string _taskDefName;
    private LHTaskSignature<T>? _taskSignature;
    private readonly T _executable;
    private readonly LittleHorseClient _lhClient;

    internal MethodInfo? TaskMethod => _taskMethod;
    internal List<VariableMapping> TaskMethodMappings => _taskMethodMappings;
    internal T Executable => _executable;
    public string TaskDefName => _taskDefName;
    internal TaskDef? TaskDef => _taskDef;

    internal LHTask(T executable, string taskDefName, LittleHorseClient lhClient)
    {
        _taskDefName = taskDefName;
        _executable = executable;
        _lhClient = lhClient;
        _taskMethodMappings = new List<VariableMapping>();
    }

    internal async Task PrepareLHTaskMethod()
    {
        _taskSignature = new LHTaskSignature<T>(_taskDefName, _executable);
        _taskMethod = _taskSignature.TaskMethod;

        await ValidateTaskMethodParameters(_taskMethod, _taskSignature);
        _taskMethodMappings = await CreateVariableMappings(_taskMethod, _taskSignature);
    }

    private async Task ValidateTaskMethodParameters(MethodInfo taskMethod, LHTaskSignature<T> taskSignature)
    {
        _taskDef = await GetTaskDef();
        if (taskSignature.HasWorkerContextAtEnd)
        {
            if (taskSignature.TaskMethod.GetParameters().Length - 1 != _taskDef.InputVars.Count)
            {
                throw new LHTaskSchemaMismatchException("Number of task method params doesn't match number of taskdef params!");
            }
        }
        else
        {
            if (taskMethod.GetParameters().Length != _taskDef.InputVars.Count)
            {
                throw new LHTaskSchemaMismatchException("Number of task method params doesn't match number of taskdef params!");
            }
        }
    }

    private async Task<List<VariableMapping>> CreateVariableMappings(MethodInfo taskMethod, LHTaskSignature<T> taskSignature)
    {
        var mappings = new List<VariableMapping>();

        var taskParams = taskMethod.GetParameters();
        _taskDef = await GetTaskDef();

        for (int index = 0; index < _taskDef?.InputVars.Count; index++)
        {
            var taskParam = taskParams[index];

            if (taskParam.ParameterType.IsAssignableFrom(typeof(LHWorkerContext)))
            {
                throw new LHTaskSchemaMismatchException("Can only have WorkerContext after all required taskDef params.");
            }

            mappings.Add(CreateVariableMapping(_taskDef, index, taskParam.ParameterType, taskParam.Name));
        }

        if (taskSignature.HasWorkerContextAtEnd)
        {
            mappings.Add(CreateVariableMapping(_taskDef, taskParams.Count() - 1, typeof(LHWorkerContext), null));
        }

        return mappings;
    }

    private VariableMapping CreateVariableMapping(TaskDef? taskDef, int index, Type type, string? paramName)
    {
        return new VariableMapping(taskDef!, index, type, paramName);
    }

    internal async Task<TaskDef> GetTaskDef()
    {
        if (_taskDef is null)
        {
            var taskDefId = new TaskDefId
            {
                Name = _taskDefName
            };
           _taskDef = await _lhClient.GetTaskDefAsync(taskDefId);
        }

        return _taskDef;
    }
}
