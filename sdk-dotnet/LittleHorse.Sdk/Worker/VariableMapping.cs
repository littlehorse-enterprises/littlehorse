using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Utils;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Sdk.Worker
{
    internal class VariableMapping
    {
        private ILogger<VariableMapping>? _logger;
        private readonly string? _name;
        private Type _type;
        private int _position;

        internal VariableMapping(TaskDef taskDef, int position, Type type, string? paramName)
        {
            _type = type;
            _name = paramName;
            _position = position;
            _logger = LHLoggerFactoryProvider.GetLogger<VariableMapping>();

            if (_type.IsAssignableFrom(typeof(LHWorkerContext)))
            {
                return;
            }

            VariableDef input = taskDef.InputVars[position];

            ValidateType(input.TypeDef, _type, _name);
        }

        internal object? Assign(ScheduledTask taskInstance, LHWorkerContext workerContext)
        {
            if (_type == typeof(LHWorkerContext))
            {
                return workerContext;
            }

            VarNameAndVal assignment = taskInstance.Variables[_position];
            string taskDefParamName = assignment.VarName;
            VariableValue val = assignment.Value;

            try
            {
                return LHMappingHelper.VariableValueToObject(val, _type);
            } catch (LHSerdeException e)
            {
                throw new LHInputVarSubstitutionException("Failed serializing Java object for variable: " + taskDefParamName + ". " + e.Message);
            }
        }
        
        private void ValidateType(TypeDefinition taskDefInputType, Type paramType, string? paramName)
        {
            string errorMsg = string.Empty;

            switch (taskDefInputType.DefinedTypeCase)
            {
                case TypeDefinition.DefinedTypeOneofCase.PrimitiveType:
                    errorMsg = ValidatePrimitiveType(taskDefInputType.PrimitiveType, paramType);
                    break;
                case TypeDefinition.DefinedTypeOneofCase.InlineArrayDef:
                    if (!(paramType.IsArray || LHMappingHelper.TryGetListElementType(paramType, out _)))
                    {
                        errorMsg = $"TaskDef provides ARRAY, func accepts {paramType.Name}";
                    }
                    break;
                case TypeDefinition.DefinedTypeOneofCase.StructDefId:
                    if (!Attribute.IsDefined(paramType, typeof(LHStructDefAttribute))
                        && paramType != typeof(Common.Proto.Struct))
                    {
                        errorMsg = $"TaskDef provides STRUCT, func accepts {paramType.Name}";
                    }
                    break;
                default:
                    throw new Exception("Not possible");
            }

            if (!string.IsNullOrEmpty(errorMsg))
            {
                errorMsg = $"Invalid assignment for var {paramName}: {errorMsg}";
                throw new LHTaskSchemaMismatchException(errorMsg);
            }
        }

        private string ValidatePrimitiveType(VariableType primitiveType, Type paramType)
        {
            switch (primitiveType)
            {
                case VariableType.Int:
                    if (!LHMappingHelper.IsInt(paramType))
                    {
                        return $"TaskDef provides INT, func accepts {paramType.Name}";
                    }

                    break;
                case VariableType.Double:
                    if (!LHMappingHelper.IsFloat(paramType))
                    {
                        return $"TaskDef provides DOUBLE, func accepts {paramType.Name}";
                    }

                    break;
                case VariableType.Str:
                    if (!paramType.IsAssignableFrom(typeof(string)))
                    {
                        return $"TaskDef provides STRING, func accepts {paramType.Name}";
                    }

                    break;
                case VariableType.Bool:
                    if (!paramType.IsAssignableFrom(typeof(bool)))
                    {
                        return $"TaskDef provides BOOL, func accepts {paramType.Name}";
                    }

                    break;
                case VariableType.Bytes:
                    if (!paramType.IsAssignableFrom(typeof(byte[])))
                    {
                        return $"TaskDef provides BYTES, func accepts {paramType.Name}";
                    }

                    break;
                case VariableType.JsonArr:
                case VariableType.JsonObj:
                    _logger?.LogInformation($"It will use Newtonsoft to deserialize Json string into {paramType.Name}");
                    break;
            }

            return string.Empty;
        }
    }
}
