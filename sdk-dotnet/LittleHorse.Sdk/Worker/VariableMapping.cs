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

            ValidateType(input.Type, _type, _name);
        }

        internal object? Assign(ScheduledTask taskInstance, LHWorkerContext workerContext)
        {
            if (_type == typeof(LHWorkerContext))
            {
                return workerContext;
            }

            VarNameAndVal assignment = taskInstance.Variables[_position];
            VariableValue val = assignment.Value;

            string? jsonStr;

            switch (val.ValueCase)
            {
                case VariableValue.ValueOneofCase.Int:
                    if (LHMappingHelper.IsInt64Type(_type))
                    {
                        return val.Int;
                    }

                    return (int) val.Int;
                case VariableValue.ValueOneofCase.Double:
                    if (_type == typeof(double) || _type == typeof(Double))
                    {
                        return val.Double;
                    }

                    return (float) val.Double;
                case VariableValue.ValueOneofCase.Str:
                    return val.Str;
                case VariableValue.ValueOneofCase.Bytes:
                    return val.Bytes.ToByteArray();
                case VariableValue.ValueOneofCase.Bool:
                    return val.Bool;
                case VariableValue.ValueOneofCase.JsonArr:
                    jsonStr = val.JsonArr;
                    return JsonHandler.DeserializeFromJson(jsonStr, _type);
                case VariableValue.ValueOneofCase.JsonObj:
                    jsonStr = val.JsonObj;
                    return JsonHandler.DeserializeFromJson(jsonStr, _type);
                default:
                    throw new InvalidOperationException("Unrecognized variable value type");
            }
        }
        
        private void ValidateType(VariableType taskDefInputType, Type paramType, string? paramName)
        {
            string errorMsg = string.Empty;

            switch (taskDefInputType)
            {
                case VariableType.Int:
                    if (!LHMappingHelper.IsInt(paramType))
                    {
                        errorMsg = $"TaskDef provides INT, func accepts {paramType.Name}";
                    }
                    break;
                case VariableType.Double:
                    if (!LHMappingHelper.IsFloat(paramType))
                    {
                        errorMsg = $"TaskDef provides DOUBLE, func accepts {paramType.Name}";
                    }
                    break;
                case VariableType.Str:
                    if (!paramType.IsAssignableFrom(typeof(string)))
                    {
                        errorMsg = $"TaskDef provides STRING, func accepts {paramType.Name}";
                    }
                    break;
                case VariableType.Bool:
                    if (!paramType.IsAssignableFrom(typeof(bool)))
                    {
                        errorMsg = $"TaskDef provides BOOL, func accepts {paramType.Name}";
                    }
                    break;
                case VariableType.Bytes:
                    if (!paramType.IsAssignableFrom(typeof(byte[])))
                    {
                        errorMsg = $"TaskDef provides BYTES, func accepts {paramType.Name}";
                    }
                    break;
                case VariableType.JsonArr:
                case VariableType.JsonObj:
                    _logger?.LogInformation($"It will use Newtonsoft to deserialize Json string into {paramType.Name}");
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
    }
}
