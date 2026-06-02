using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Worker
{
    internal sealed class LHTaskReturnType
    {
        private readonly ReturnType _returnType;

        internal ReturnType ReturnType => _returnType;

        internal LHTaskReturnType(MethodInfo taskMethod)
        {
            if (taskMethod.ReturnType == typeof(void) || taskMethod.ReturnType == typeof(Task))
            {
                _returnType = new ReturnType { };
                return;
            }

            if (!taskMethod.ReturnType.IsGenericType
                || taskMethod.ReturnType.GetGenericTypeDefinition() != typeof(Task<>))
            {
                throw new LHTaskSchemaMismatchException("Task methods must return Task<type>, Task, or void");
            }

            Type returnType = taskMethod.ReturnType.GetGenericArguments().First();
            LHTypeMetadata metadata = LHTypeMetadata.From(taskMethod);
            metadata.ValidateLHArrayUsage(returnType, LHTypeMetadata.ValidationContext.ReturnType, taskMethod.Name);

            LHClassType lhClassType = LHTypeMetadata.ResolveTaskDefinitionType(returnType, metadata.IsLHArray);
            TypeDefinition typeDef = lhClassType.GetTypeDefinition();
            typeDef.Masked = metadata.Masked;

            _returnType = new ReturnType
            {
                ReturnType_ = typeDef
            };
        }
    }
}