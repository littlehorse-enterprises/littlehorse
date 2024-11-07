using LittleHorse.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Sdk.Worker
{
    public class VariableMapping
    {
        private ILogger? _logger;
        private string? _name;
        private Type _type;
        private int _position;

        public VariableMapping(TaskDef taskDef, int position, Type type, string? paramName)
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

        public object? Assign(ScheduledTask taskInstance, LHWorkerContext workerContext)
        {
            if (_type.GetType() == typeof(LHWorkerContext))
            {
                return workerContext;
            }

            VarNameAndVal assignment = taskInstance.Variables[_position];
            string taskDefParamName = assignment.VarName;
            VariableValue val = assignment.Value;

            string? jsonStr;

            switch (val.ValueCase)
            {
                case VariableValue.ValueOneofCase.Int:
                    if (_type == typeof(long) || _type == typeof(long?))
                    {
                        return val.Int;
                    }

                    return (int)val.Int;
                case VariableValue.ValueOneofCase.Double:
                    if (_type == typeof(double) || _type == typeof(double?))
                    {
                        return val.Double;
                    }

                    return (float)val.Double;
                case VariableValue.ValueOneofCase.Str:
                    return val.Str;
                case VariableValue.ValueOneofCase.Bytes:
                    return val.Bytes.ToByteArray();
                case VariableValue.ValueOneofCase.Bool:
                    return val.Bool;
                case VariableValue.ValueOneofCase.JsonArr:
                    jsonStr = val.JsonArr;
                    break;
                case VariableValue.ValueOneofCase.JsonObj:
                    jsonStr = val.JsonObj;
                    break;
                default:
                    throw new InvalidOperationException("Unrecognized variable value type");
            }

            try
            {
                return LHMappingHelper.DeserializeFromJson(jsonStr, _type);
            }
            catch (Exception ex)
            {
                throw new LHInputVarSubstitutionException($"Failed deserializing the C# object for variable {taskDefParamName}", ex);
            }
        }


        private void ValidateType(VariableType taskDefInputType, Type paramType, string? paramName)
        {
            string errorMsg = string.Empty;

            switch (taskDefInputType)
            {
                case VariableType.Int:
                    if (!paramType.IsAssignableFrom(typeof(int)))
                    {
                        errorMsg = $"TaskDef provides INT, func accepts {paramType.Name}";
                    }
                    break;
                case VariableType.Double:
                    if (!paramType.IsAssignableFrom(typeof(double)))
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
                    _logger?.LogInformation($"Will use Jackson to deserialize Json into {paramType.Name}");
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
