using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Worker.Internal;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Sdk.Worker
{
    public class LHTaskSignature<T>
    {
        private readonly List<LHMethodParam> _lhMethodParams;
        internal List<LHMethodParam> LhMethodParams => _lhMethodParams;
        public MethodInfo TaskMethod { get; init; }
        public bool HasWorkerContextAtEnd { get; set; }
        public string TaskDefName { get; init; }
        public T? Executable { get; init; }
        
        private ILogger<LHTaskSignature<T>?> _logger;

        private TaskDefOutputSchema? _taskDefOutputSchema;

        internal TaskDefOutputSchema? TaskDefOutputSchema => _taskDefOutputSchema;

        public LHTaskSignature(string taskDefName, T executable)
        {
            _logger = LHLoggerFactoryProvider.GetLogger<LHTaskSignature<T>>();
            _lhMethodParams = new List<LHMethodParam>();
            TaskDefName = taskDefName;
            Executable = executable;

            var methodsWithTaskWorkerAttrsAndDefName = typeof(T).GetMethods()
                         .Where(method => method.CustomAttributes.Any(ca => 
                             ca.AttributeType == typeof(LHTaskMethodAttribute) 
                             || ca.AttributeType == typeof(LHTypeAttribute)))
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

            var methodParams = TaskMethod.GetParameters();

            BuildInputVarsSignature(methodParams);

            if (!TaskMethod.ReturnType.IsAssignableFrom(typeof(void)))
                BuildOutputSchemaSignature();
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
            for (int i = 0; i < methodParams.Length; i++)
            {
                var paramType = methodParams[i].ParameterType;
                var defaultParamName = methodParams[i].Name;
                HasWorkerContextAtEnd = false;

                if (paramType == typeof(LHWorkerContext))
                {
                    if (i != methodParams.Count() - 1)
                    {
                        throw new LHTaskSchemaMismatchException("Can only have WorkerContext as the last parameter.");
                    }

                    HasWorkerContextAtEnd = true;
                    continue;
                }

                var paramLHType = LHMappingHelper.DotNetTypeToLHVariableType(paramType);

                bool maskedParam = false;
                var paramName = defaultParamName; 
                
                if (methodParams[i].GetCustomAttribute(typeof(LHTypeAttribute)) is LHTypeAttribute lhTypeValue)
                {
                    maskedParam = lhTypeValue.Masked;
                    if (!lhTypeValue.Name.Trim().Equals(string.Empty))
                        paramName = lhTypeValue.Name;
                }

                LHMethodParam lhMethodParam = new LHMethodParam
                {
                    Type = paramLHType,
                    Name = paramName,
                    IsMasked = maskedParam
                };
                
                _lhMethodParams.Add(lhMethodParam);
            }
        }
        
        private void BuildOutputSchemaSignature()
        {
            var returnType = LHMappingHelper.DotNetTypeToLHVariableType(TaskMethod.ReturnType);
            var maskedValue = false;
            string outputSchemaVarName = "output";

            if (TaskMethod.GetCustomAttribute(typeof(LHTypeAttribute)) is LHTypeAttribute lhType) {
                maskedValue = lhType!.Masked;
                if (lhType.Name.Trim() != "") {
                    outputSchemaVarName = lhType.Name;
                }
            }

            var variableDef = new VariableDef
            {
                Name = outputSchemaVarName,
                Type = returnType,
                MaskedValue = maskedValue
            };

            _taskDefOutputSchema = new TaskDefOutputSchema
            {
                ValueDef = variableDef
            };
        }
    }
}
