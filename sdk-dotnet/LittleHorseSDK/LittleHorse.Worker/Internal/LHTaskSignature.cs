using LittleHorse.Common.Exceptions;
using LittleHorse.Worker.Attributes;
using LittleHorse.Worker.Internal.Helpers;
using LittleHorseSDK.Common.proto;
using System.Reflection;

namespace LittleHorse.Worker.Internal
{
    public class LHTaskSignature<T>
    {
        public List<VariableType> ParamTypes { get; init; }
        public List<string?> VarNames { get; init; }
        public MethodInfo TaskMethod { get; init; }
        public bool HasWorkerContextAtEnd { get; init; } = false;
        public string TaskDefName { get; init; }
        public T? Executable { get; init; }

        public LHTaskSignature(string taskDefName, T executable)
        {
            ParamTypes = new List<VariableType>();
            VarNames = new List<string?>();

            TaskDefName = taskDefName;
            Executable = executable;

            var methodsWithTaskWorkerAttrAndDefName = typeof(T).GetMethods()
                         .Where(method => method.CustomAttributes.Any(ca => ca.AttributeType == typeof(LHTaskMethodAttribute)))
                         .Where(method => IsValidLHTaskWorkerValue(method.GetCustomAttribute(typeof(LHTaskMethodAttribute)), taskDefName));

            if (!methodsWithTaskWorkerAttrAndDefName.Any())
            {
                throw new LHTaskSchemaMismatchException($"Couldn't find [LHTaskMethod] attribute for taskDef {taskDefName} on {typeof(T).Name}");
            }

            if (methodsWithTaskWorkerAttrAndDefName.Count() > 1)
            {
                throw new LHTaskSchemaMismatchException("Found two annotated task methods!");
            }

            TaskMethod = methodsWithTaskWorkerAttrAndDefName.Single();

            var methodParams = TaskMethod.GetParameters();

            for (int i = 0; i < methodParams.Length; i++)
            {
                var paramType = methodParams[i].ParameterType;
                var paramName = methodParams[i].Name;

                if (paramType == typeof(LHWorkerContext))
                {
                    if (i != methodParams.Count() - 1)
                    {
                        throw new LHTaskSchemaMismatchException("Can only have WorkerContext as the last parameter.");
                    }

                    HasWorkerContextAtEnd = true;
                    continue;
                }

                var paramLHType = LHMappingHelper.MapDotNetTypeToLHVariableType(paramType);

                ParamTypes.Add(paramLHType);
                VarNames.Add(paramName);
            }
        }


        private bool IsValidLHTaskWorkerValue(Attribute? lhtaskWorkerAttribute, string taskDefName)
        {
            if (lhtaskWorkerAttribute is LHTaskMethodAttribute a)
            {
                return a.Value == taskDefName;
            }

            return false;
        }
    }
}
