using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using LittleHorseSDK.Common.proto;
using Newtonsoft.Json;

namespace LittleHorse.Worker.Internal.Helpers
{
    public static class LHMappingHelper
    {
        public static VariableType MapDotNetTypeToLHVariableType(System.Type type)
        {
            if (type.IsAssignableFrom(typeof(int)))
            {
                return VariableType.Int;
            }
            else if (type.IsAssignableFrom(typeof(double)))
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
            else if (type.IsArray)
            {
                return VariableType.JsonArr;
            }
            else
            {
                return VariableType.JsonObj;
            }
        }
        public static DateTime? MapDateTimeFromProtoTimeStamp(Timestamp protoTimestamp)
        {
            DateTime? outDate = null;

            if (protoTimestamp == null) return null;

            outDate = DateTimeOffset.FromUnixTimeSeconds(protoTimestamp.Seconds).DateTime;
            outDate = outDate?.AddMilliseconds(protoTimestamp.Nanos / 1_000_000.0);

            if (outDate?.Ticks == 0)
            {
                outDate = DateTime.Now;
            }

            return outDate;
        }
        public static string ObjectSerializeToJson(object o)
        {
            var jsonSettings = new JsonSerializerSettings();

            return JsonConvert.SerializeObject(o, jsonSettings);
        }
        public static object? DeserializeFromJson(string json, System.Type type)
        {
            return JsonConvert.DeserializeObject(json, type);
        }
        public static VariableValue MapObjectToVariableValue(object? o)
        {
            if (o is VariableValue variableValue) return variableValue;

            var result = new VariableValue();
            if (o == null)
            {
                result.Type = VariableType.Null;
            }
            else if (o is long longValue)
            {
                result.Type = VariableType.Int;
                result.Int = longValue;
            }
            else if (o is int intValue)
            {
                result.Type = VariableType.Int;
                result.Int = intValue;
            }
            else if (o is double doubleValue)
            {
                result.Type = VariableType.Double;
                result.Double = doubleValue;
            }
            else if (o is float floatValue)
            {
                result.Type = VariableType.Double;
                result.Double = floatValue;
            }
            else if (o is string stringValue)
            {
                result.Type = VariableType.Str;
                result.Str = stringValue;
            }
            else if (o is bool boolValue)
            {
                result.Type = VariableType.Bool;
                result.Bool = boolValue;
            }
            else if (o is byte[] byteArray)
            {
                result.Type = VariableType.Bytes;
                result.Bytes = ByteString.CopyFrom(byteArray);
            }
            else
            {
                // At this point, all we can do is try to make it a JSON type.
                var jsonStr = ObjectSerializeToJson(o);

                if (o is IList<object> list)
                {
                    result.Type = VariableType.JsonArr;
                    result.JsonArr = jsonStr;
                }
                else
                {
                    result.Type = VariableType.JsonObj;
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
                    Type = VariableType.Str
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
    }
}
