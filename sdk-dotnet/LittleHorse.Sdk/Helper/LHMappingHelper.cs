using System.Collections;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Utils;
using LittleHorse.Sdk.Worker;
using Newtonsoft.Json.Linq;
using TaskStatus = LittleHorse.Sdk.Common.Proto.TaskStatus;
using Type = System.Type;

namespace LittleHorse.Sdk.Helper
{
    /// <summary>
    /// Helper class to transform LittleHorse objects into Dotnet objects
    /// </summary>
    public static class LHMappingHelper
    {
        /// <summary>
        /// Converts a Dotnet type to a LittleHorse VariableType.
        /// This method returns LH <c>JsonObj</c> type if the dotnet type is not 64-bit integers and floating points,
        ///  <c>strings</c>, <c>bool</c>, <c>bytes</c>, <c>objects</c> and the family of <c>Ilist</c>
        /// 
        /// </summary>
        /// <param name="type"> The LH type to convert.</param>
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
        
        /// <summary>
        /// Converts a Timestamp from the proto lib to Dotnet Datetime.
        /// 
        /// </summary>
        /// <param name="protoTimestamp"> The timestamp to convert.</param>
        public static DateTime? DateTimeFromProtoTimeStamp(Timestamp? protoTimestamp)
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
        
        /// <summary>
        /// Converts a dotnet object to LH VariableValue.
        /// If the object already is a VariableValue, it will return the same value.
        /// The object will be parsed to LH <c>JsonArr</c> if it is an <c>Ilist</c> type
        /// 
        /// </summary>
        /// <param name="obj"> The object to convert.</param>
        /// <exception cref="LHJsonProcessingException">
        /// It will throw an exception if the object can not be serialized. 
        /// </exception>
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

                if (obj is IList)
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
        
        /// <summary>
        /// Concatenates the <c>exception.StackTrace</c> and the <c>LogOutput</c> coming from <c>LHWorkerContext</c>
        /// to an only string variable value.
        /// 
        /// </summary>
        /// <param name="exception"> The exception to add to VariableValue.</param>
        /// <param name="ctx"> The worker context that contains a LogOutput to add to VariableValue.</param>
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
        
        /// <summary>
        /// Converts a proto message to json string.
        /// 
        /// </summary>
        /// <param name="o"> It is the proto message such as LH VariableValue.</param>
        public static string? ProtoToJson(IMessage o)
        {
            try
            {
                var jsonFormatter = new JsonFormatter(new JsonFormatter.Settings(true));
                string json = jsonFormatter.Format(o);
                var jObject = JObject.Parse(json);
                var orderedJObject = new JObject(jObject.Properties().OrderBy(p => p.Name));
                
                return orderedJObject.ToString();
            }
            catch (InvalidProtocolBufferException ex)
            {
                Console.WriteLine(ex);
                return null;
            }
        }

        /// <summary>
        /// Converts a LH variable value type to a LH variable type.
        /// <c>JsonObj</c> and <c>None</c> ValueCase will be mapped to VariableType.JsonObj
        /// 
        /// </summary>
        /// <param name="valueCase"> The valueCase which can contain any of the LH allowed types.
        /// <c>JsonObj, JsonArr, Double, Bool, Str, Int, Bytes</c>
        /// </param>
        public static VariableType ValueCaseToVariableType(VariableValue.ValueOneofCase valueCase)
        {
            switch (valueCase) {
                case VariableValue.ValueOneofCase.Str:
                    return VariableType.Str;
                case VariableValue.ValueOneofCase.Bytes:
                    return VariableType.Bytes;
                case VariableValue.ValueOneofCase.Int:
                    return VariableType.Int;
                case VariableValue.ValueOneofCase.Double:
                    return VariableType.Double;
                case VariableValue.ValueOneofCase.Bool:
                    return VariableType.Bool;
                case VariableValue.ValueOneofCase.JsonArr:
                    return VariableType.JsonArr;
                case VariableValue.ValueOneofCase.JsonObj:
                case VariableValue.ValueOneofCase.None:
                default:
                    return VariableType.JsonObj;
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
            switch (status) 
            {
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
