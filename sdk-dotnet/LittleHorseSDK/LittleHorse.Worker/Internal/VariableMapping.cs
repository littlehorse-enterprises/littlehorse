using LittleHorse.Common.Exceptions;
using LittleHorse.Worker.Internal.Helpers;
using LittleHorseSDK.Common.proto;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Worker.Internal
{
    public class VariableMapping
    {
        private ILogger? _logger;
        private string? _name;
        private Type _type;
        private int _position;

        public VariableMapping(TaskDef taskDef, int position, Type type, string? paramName, ILogger? logger = null)
        {
            _type = type;
            _name = paramName;
            _position = position;
            _logger = logger;

            if (_type.IsAssignableFrom(typeof(LHWorkerContext)))
            {
                return;
            }

            var input = taskDef.InputVars[position];

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

            switch (val.Type)
            {
                case VariableType.Int:
                    if (_type == typeof(long) || _type == typeof(long?))
                    {
                        return val.Int;
                    }
                    else
                    {
                        return (int)val.Int;
                    }
                case VariableType.Double:
                    if (_type == typeof(double) || _type == typeof(double?))
                    {
                        return val.Double;
                    }
                    else
                    {
                        return (float)val.Double;
                    }
                case VariableType.Str:
                    return val.Str;
                case VariableType.Bytes:
                    return val.Bytes.ToByteArray();
                case VariableType.Bool:
                    return val.Bool;
                case VariableType.JsonArr:
                    jsonStr = val.JsonArr;
                    break;
                case VariableType.Null:
                    return null;
                case VariableType.JsonObj:
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
                case VariableType.Null:
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
