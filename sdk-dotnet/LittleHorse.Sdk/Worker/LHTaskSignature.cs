using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Sdk.Worker
{
    internal class LHTaskSignature<T>
    {
        private readonly List<VariableDef> _variableDefs;
        internal List<VariableDef> VariableDefs => _variableDefs;
        internal MethodInfo TaskMethod { get; init; }
        internal bool HasWorkerContextAtEnd { get; set; }
        internal string TaskDefName { get; init; }
        internal T? Executable { get; init; }
        internal string? TaskDefDescription { get; private set; }
        
        private ILogger<LHTaskSignature<T>?> _logger;

        private ReturnType? _outputSchema;

        internal ReturnType? ReturnType => _outputSchema;

        internal LHTaskSignature(string taskDefName, T executable)
        {
            _logger = LHLoggerFactoryProvider.GetLogger<LHTaskSignature<T>>();
            _variableDefs = new List<VariableDef>();
            TaskDefName = taskDefName;
            Executable = executable;

            var methodsWithTaskWorkerAttrsAndDefName = typeof(T).GetMethods()
                .Where(method => method.GetCustomAttribute(typeof(LHTaskMethodAttribute)) is LHTaskMethodAttribute)
                .Where(method => IsValidLHTaskWorkerValue(
                    method.GetCustomAttribute(typeof(LHTaskMethodAttribute)), taskDefName));
            
            if (!methodsWithTaskWorkerAttrsAndDefName.Any())
            {
                throw new LHTaskSchemaMismatchException($"Couldn't find [LHTaskMethod] attribute for taskDef {taskDefName} on {typeof(T).Name}");
            }

            if (methodsWithTaskWorkerAttrsAndDefName.Count() > 1)
            {
                throw new LHTaskSchemaMismatchException("Found more than one annotated task methods!");
            }

            TaskMethod = methodsWithTaskWorkerAttrsAndDefName.Single();

            if (TaskMethod.GetCustomAttribute(typeof(LHTaskMethodAttribute)) is LHTaskMethodAttribute lhTaskMethodAttr
                && !string.IsNullOrWhiteSpace(lhTaskMethodAttr.Description))
            {
                TaskDefDescription = lhTaskMethodAttr.Description;
            }

            var methodParams = TaskMethod.GetParameters();

            BuildInputVarsSignature(methodParams);

            _outputSchema = BuildReturnType();
        }
        
        private bool IsValidLHTaskWorkerValue(Attribute? lhtaskWorkerAttribute, string taskDefName)
        {
            if (lhtaskWorkerAttribute is LHTaskMethodAttribute attr)
            {
                return attr.Value == taskDefName;
            }

            return false;
        }

        private void BuildInputVarsSignature(ParameterInfo[] methodParams)
        {
            HasWorkerContextAtEnd = false;
            for (int i = 0; i < methodParams.Length; i++)
            {
                var paramType = methodParams[i].ParameterType;

                if (paramType == typeof(LHWorkerContext))
                {
                    if (i != methodParams.Count() - 1)
                    {
                        throw new LHTaskSchemaMismatchException("Can only have WorkerContext as the last parameter.");
                    }

                    HasWorkerContextAtEnd = true;
                    continue;
                }

                var taskParameter = new LHTaskParameter(methodParams[i], i, _logger);
                _variableDefs.Add(taskParameter.VariableDef);
            }
        }
        
        private ReturnType BuildReturnType()
        {
            var taskReturnType = new LHTaskReturnType(TaskMethod);
            return taskReturnType.ReturnType;
        }
    }
}
