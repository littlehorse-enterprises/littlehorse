using System.Collections;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Utils;
using LittleHorse.Sdk.Worker;
using TaskStatus = LittleHorse.Sdk.Common.Proto.TaskStatus;
using Type = System.Type;

namespace LittleHorse.Sdk.Helper
{
    public static class LHMappingHelper
    {
        public static VariableType DotNetTypeToLHVariableType(Type type)
        {
            if (IsInt(type))
            {
                return VariableType.Int;
            }

            if (IsFloat(type))
            {
                return VariableType.Double;
            }

            if (type.IsAssignableFrom(typeof(string)))
            {
                return VariableType.Str;
            }

            if (type.IsAssignableFrom(typeof(bool)))
            {
                return VariableType.Bool;
            }

            if (type.IsAssignableFrom(typeof(byte[])))
            {
                return VariableType.Bytes;
            }

            if (typeof(IList).IsAssignableFrom(type))
            {
                return VariableType.JsonArr;
            }

            return VariableType.JsonObj;
        }
        
        public static DateTime? DateTimeFromProtoTimeStamp(Timestamp protoTimestamp)
        {
            if (protoTimestamp == null) return null;

            DateTime? outDate = DateTimeOffset.FromUnixTimeSeconds(protoTimestamp.Seconds).DateTime;
            outDate = outDate?.AddMilliseconds(protoTimestamp.Nanos / 1_000_000.0);

            if (protoTimestamp is { Seconds: 0, Nanos: 0 })
            {
                return DateTime.Now;
            }

            return outDate;
        }
        
        public static VariableValue ObjectToVariableValue(object? obj)
        {
            if (obj is VariableValue variableValue) return variableValue;

            var result = new VariableValue();
            if (obj == null) {}
            else if (IsIntObject(obj))
            {
                result.Int = GetIntegralValue(obj);
            }
            else if (IsDoubleObject(obj))
            {
                result.Double = GetFloatingValue(obj);
            }
            else if (obj is string stringValue)
            {
                result.Str = stringValue;
            }
            else if (obj is bool boolValue)
            {
                result.Bool = boolValue;
            }
            else if (obj is byte[] byteArray)
            {
                result.Bytes = ByteString.CopyFrom(byteArray);
            }
            else
            {
                var jsonStr = JsonHandler.ObjectSerializeToJson(obj);

                if (obj is IEnumerable)
                {
                    result.JsonArr = jsonStr;
                }
                else
                {
                    result.JsonObj = jsonStr;
                }
            }

            return result;
        }
        
        public static VariableValue ExceptionToVariableValue(Exception exception, LHWorkerContext ctx)
        {
            using (StringWriter sw = new StringWriter())
            {
                using (TextWriter pw = sw)
                {
                    pw.WriteLine(exception.StackTrace);
                }

                string output = sw.ToString();

                if (ctx.LogOutput != null)
                {
                    output += "\n\n\n\n" + ctx.LogOutput;
                }

                return new VariableValue()
                {
                    Str = output,
                };
            }
        }
        
        public static string? ProtoToJson(IMessage o)
        {
            try
            {
                var jsonFormatter = new JsonFormatter(new JsonFormatter.Settings(true));
                return jsonFormatter.Format(o);
            }
            catch (InvalidProtocolBufferException ex)
            {
                Console.WriteLine(ex);
                return null;
            }
        }

        internal static bool IsFloat(Type type)
        {
            return type.IsAssignableFrom(typeof(float))
                || type.IsAssignableFrom(typeof(double))
                || type.IsAssignableFrom(typeof(Double));
        }

        internal static bool IsInt(Type type)
        {
            return type.IsAssignableFrom(typeof(sbyte))
                || type.IsAssignableFrom(typeof(byte))
                || type.IsAssignableFrom(typeof(short))
                || type.IsAssignableFrom(typeof(ushort))
                || type.IsAssignableFrom(typeof(int))
                || type.IsAssignableFrom(typeof(uint))
                || type.IsAssignableFrom(typeof(long))
                || type.IsAssignableFrom(typeof(ulong))
                || type.IsAssignableFrom(typeof(nint))
                || type.IsAssignableFrom(typeof(nuint));
        }
        
        private static bool IsIntObject(object obj)
        {
            return obj is sbyte or byte or short or ushort or int or uint or long or ulong or nint or nuint;
        }
        
        private static bool IsDoubleObject(object obj)
        {
            return obj is double or float or Double;
        }
        
        private static long GetIntegralValue(object obj)
        {
            return obj switch
            {
                sbyte sb => sb,
                byte b => b,
                short s => s,
                ushort us => us,
                int i => i,
                uint ui => ui,
                long l => l,
                ulong ul => (long)ul,
                nint ni => ni,
                nuint nui => (long)nui,
                _ => throw new LHInputVarSubstitutionException("Object value can not be converted to a long.")
            };
        }
        
        private static Double GetFloatingValue(object obj)
        {
            return obj switch
            {
                double d => d,
                float f => f,
                _ => throw new LHInputVarSubstitutionException("Object value can not be converted to a Double.")
            };
        }

        internal static bool IsInt64Type(Type type)
        {
            return type.IsAssignableFrom(typeof(Int64))
                   || type.IsAssignableFrom(typeof(UInt64))
                   || type.IsAssignableFrom(typeof(long))
                   || type.IsAssignableFrom(typeof(ulong));
        }
        
        internal static LHErrorType GetFailureCodeFor(TaskStatus status)
        {
            switch (status) {
                case TaskStatus.TaskFailed:
                    return LHErrorType.TaskFailure;
                case TaskStatus.TaskTimeout:
                    return LHErrorType.Timeout;
                case TaskStatus.TaskOutputSerializingError:
                    return LHErrorType.VarMutationError;
                case TaskStatus.TaskInputVarSubError:
                    return LHErrorType.VarSubError;
                case TaskStatus.TaskRunning:
                case TaskStatus.TaskScheduled:
                case TaskStatus.TaskSuccess:
                case TaskStatus.TaskPending:
                case TaskStatus.TaskException: // TASK_EXCEPTION is NOT a technical ERROR, so this fails.
                    break;
            }

            throw new ArgumentException($"Unexpected task status: {status}");;
        }
    }
}
