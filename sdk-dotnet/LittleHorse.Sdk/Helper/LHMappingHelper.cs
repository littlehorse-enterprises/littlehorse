﻿using System.Collections;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using LittleHorse.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Utils;
using LittleHorse.Sdk.Worker;
using Type = System.Type;

namespace LittleHorse.Sdk.Helper
{
    public static class LHMappingHelper
    {
        public static VariableType MapDotNetTypeToLHVariableType(Type type)
        {
            if (IsInt(type))
            {
                return VariableType.Int;
            }
            else if (IsFloat(type))
            {
                return VariableType.Double;
            }
            else if (type.IsAssignableFrom(typeof(string)))
            {
                return VariableType.Str;
            }
            else if (type.IsAssignableFrom(typeof(bool)))
            {
                return VariableType.Bool;
            }
            else if (type.IsAssignableFrom(typeof(byte[])))
            {
                return VariableType.Bytes;
            }
            else if (typeof(IEnumerable).IsAssignableFrom(type))
            {
                return VariableType.JsonArr;
            }
            else if (!type.Namespace!.StartsWith("System"))
            {
                return VariableType.JsonObj;
            }
            else
            {
                throw new Exception("Unaccepted variable type.");
            }
        }
        
        public static DateTime? MapDateTimeFromProtoTimeStamp(Timestamp protoTimestamp)
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
        
        public static VariableValue MapObjectToVariableValue(object? obj)
        {
            if (obj is VariableValue variableValue) return variableValue;

            var result = new VariableValue();
            if (obj == null)
            {
                throw new LHInputVarSubstitutionException("There is no object to be mapped.");
            }
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
        
        public static VariableValue MapExceptionToVariableValue(Exception exception, LHWorkerContext ctx)
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
        
        public static string? MapProtoToJson(IMessage o)
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

        public static bool isInt64Type(Type type)
        {
            return type.IsAssignableFrom(typeof(Int64))
                   || type.IsAssignableFrom(typeof(UInt64))
                   || type.IsAssignableFrom(typeof(long))
                   || type.IsAssignableFrom(typeof(ulong));
        }
    }
}
