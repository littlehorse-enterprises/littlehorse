using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
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

                _variableDefs.Add(BuildVariableDef(methodParams[i], i));
            }
        }

        private VariableDef BuildVariableDef(ParameterInfo param, int index)
        {
            var paramType = param.ParameterType;
            var lhClassType = LHClassType.FromType(paramType);
            var typeDef = lhClassType.GetTypeDefinition();

            var maskedParam = false;
            var paramName = param.Name;
            if (string.IsNullOrWhiteSpace(paramName))
            {
                _logger.LogWarning("Unable to inspect parameter names using reflection; using parameter index as name.");
                paramName = $"param{index}";
            }

            if (param.GetCustomAttribute(typeof(LHTypeAttribute)) is LHTypeAttribute lhTypeValue)
            {
                maskedParam = lhTypeValue.Masked;
                if (!lhTypeValue.Name.Trim().Equals(string.Empty))
                {
                    paramName = lhTypeValue.Name;
                }
            }

            typeDef.Masked = maskedParam;

            return new VariableDef
            {
                Name = paramName,
                TypeDef = typeDef
            };
        }
        
        private ReturnType BuildReturnType()
        {
            if (TaskMethod.ReturnType == typeof(void) || TaskMethod.ReturnType == typeof(Task))
            {
                return new ReturnType { };
            }

            if (!TaskMethod.ReturnType.IsGenericType
                || TaskMethod.ReturnType.GetGenericTypeDefinition() != typeof(Task<>))
            {
                throw new LHTaskSchemaMismatchException("Task methods must return Task<type>, Task, or void");
            }

            var returnType = TaskMethod.ReturnType.GetGenericArguments().First();
            var lhClassType = LHClassType.FromType(returnType);
            var typeDef = lhClassType.GetTypeDefinition();
            var maskedValue = false;

            if (TaskMethod.GetCustomAttribute(typeof(LHTypeAttribute)) is LHTypeAttribute lhType)
            {
                maskedValue = lhType.Masked;
            }

            typeDef.Masked = maskedValue;

            return new ReturnType
            {
                ReturnType_ = typeDef
            };
        }
    }
}
