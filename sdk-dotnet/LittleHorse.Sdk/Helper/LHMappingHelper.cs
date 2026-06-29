using System.Collections;
using System.Linq;
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
    /// Helper class to handle transformations of LittleHorse or dotnet objects.
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
            else if (obj is Common.Proto.Struct lhStruct)
            {
                result.Struct = lhStruct;
            }
            else if (Attribute.IsDefined(obj.GetType(), typeof(LHStructDefAttribute)))
            {
                result.Struct = SerializeToStruct(obj);
            }
            else if (obj is DateTime dateTime) 
            {
                result.UtcTimestamp = Timestamp.FromDateTime(dateTime.ToUniversalTime());
            }
            else if (obj is WfRunId wfRunId)
            {
                result.WfRunId = wfRunId;
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
        /// Serializes a .NET array/list into a native LittleHorse ARRAY VariableValue.
        /// This mirrors the Java <c>objToVarValAsNativeArray</c> behavior as an explicit API.
        /// </summary>
        /// <param name="obj">The array/list object to serialize.</param>
        /// <param name="declaredArrayType">
        /// The declared .NET type of the array or list (e.g. <c>typeof(string[])</c> or <c>typeof(List&lt;string&gt;)</c>).
        /// When provided, the element type is resolved from this type and used to serialize each item,
        /// mirroring the Java SDK's type-safe element serialization via the declared component type.
        /// </param>
        internal static VariableValue ObjectToVariableValueAsNativeArray(object? obj, Type? declaredArrayType = null)
        {
            if (obj == null)
            {
                return new VariableValue();
            }

            if (obj is byte[])
            {
                throw new LHSerdeException("Cannot serialize byte[] as native ARRAY. Use BYTES instead.");
            }

            if (obj is System.Array nativeArray)
            {
                Type? elementType = declaredArrayType?.GetElementType();
                return new VariableValue
                {
                    Array = ToNativeArray(nativeArray, elementType)
                };
            }

            if (obj is IList list)
            {
                Type? elementType = declaredArrayType != null && TryGetListElementType(declaredArrayType, out Type et) ? et : null;
                return new VariableValue
                {
                    Array = ToNativeArray(list, elementType)
                };
            }

            throw new LHSerdeException(
                $"Native array serialization requires an array or IList value, but got [{obj.GetType().Name}].");
        }

        /// <summary>
        /// Converts an LH VariableValue to a C# object
        /// </summary>
        /// <param name="val">The value</param>
        /// <param name="type">The type of the desired C# object</param>
        /// <returns>The converted C# object</returns>
        /// <exception cref="InvalidOperationException">
        /// Throws an exception if the VariableValue type is not recognized
        /// </exception>
        public static object? VariableValueToObject(VariableValue val, Type type)
        {
            string? jsonStr;

            switch (val.ValueCase)
            {
                case VariableValue.ValueOneofCase.Int:
                    if (IsInt64Type(type))
                    {
                        return val.Int;
                    }

                    return (int) val.Int;
                case VariableValue.ValueOneofCase.Double:
                    if (type == typeof(double))
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
                case VariableValue.ValueOneofCase.Struct:
                    return DeserializeStructToObject(val.Struct, type);
                case VariableValue.ValueOneofCase.Array:
                    return DeserializeNativeArrayToObject(val.Array, type);
                case VariableValue.ValueOneofCase.JsonArr:
                    jsonStr = val.JsonArr;
                    return JsonHandler.DeserializeFromJson(jsonStr, type);
                case VariableValue.ValueOneofCase.JsonObj:
                    jsonStr = val.JsonObj;
                    return JsonHandler.DeserializeFromJson(jsonStr, type);
                case VariableValue.ValueOneofCase.UtcTimestamp:
                    return val.UtcTimestamp.ToDateTime();
                case VariableValue.ValueOneofCase.WfRunId:
                    return val.WfRunId;
                case VariableValue.ValueOneofCase.None:
                    return null;
                default:
                    throw new InvalidOperationException("Unrecognized variable value type");
            }
        }

        /// <summary>
        /// Deserializes a LittleHorse Struct into a C# object of the requested type.
        /// </summary>
        /// <param name="val">The Struct value to convert.</param>
        /// <param name="type">The target C# type.</param>
        /// <returns>An instance of <paramref name="type" /> populated from the Struct fields.</returns>
        private static object? DeserializeStructToObject(Common.Proto.Struct val, Type type)
        {
            var lhClassType = LHClassType.FromType(type);

            if (lhClassType is not LHStructDefType structDefType)
            {
                throw new LHSerdeException("Failed deserializing Struct into class of type: " + lhClassType.GetType().Name);
            }

            try
            {
                var structObject = lhClassType.CreateInstance();
                if (structObject == null)
                {
                    throw new LHSerdeException("Failed deserializing Struct into Object: could not create instance for type " + type.FullName);
                }

                var inlineStruct = val.Struct_ ?? new Common.Proto.InlineStruct();
                foreach (var property in structDefType.GetStructProperties())
                {
                    string fieldName = property.FieldName;
                    if (!inlineStruct.Fields.ContainsKey(fieldName))
                    {
                        throw new LHSerdeException(
                            string.Format(
                                "Failed deserializing VariableValue into Struct because no such field [{0}] exists on class [{1}]",
                                fieldName,
                                type.FullName));
                    }

                    VariableValue fieldValue = inlineStruct.Fields[fieldName].Value;
                    property.SetValueTo(structObject, fieldValue);
                }

                return structObject;
            }
            catch (Exception ex) when (ex is not LHSerdeException)
            {
                throw new LHSerdeException("Failed deserializing Struct into Object", ex);
            }
        }

        /// <summary>
        /// Serializes a C# object to a LittleHorse Struct based on its struct definition metadata.
        /// </summary>
        /// <param name="o">The source object.</param>
        /// <returns>A Struct whose fields mirror the object's properties.</returns>
        /// <exception cref="InvalidOperationException">Thrown when the object type is not a struct definition.</exception>
        private static Common.Proto.Struct SerializeToStruct(object o)
        {
            var lhClassType = LHClassType.FromType(o.GetType());

            if (lhClassType is not LHStructDefType structDefType)
            {
                throw new InvalidOperationException("Cannot serialize given object to Struct");
            }

            var outputStruct = new Common.Proto.Struct
            {
                StructDefId = structDefType.GetStructDefId()
            };

            var inlineStruct = new Common.Proto.InlineStruct();

            try
            {
                foreach (var property in structDefType.GetStructProperties())
                {
                    VariableValue? fieldValue = property.GetValueFrom(o);
                    if (fieldValue == null)
                    {
                        continue;
                    }

                    var structField = new Common.Proto.StructField
                    {
                        Value = fieldValue
                    };

                    inlineStruct.Fields.Add(property.FieldName, structField);
                }
            }
            catch (Exception ex)
            {
                throw new LHSerdeException("Failed serializing object to Struct: " + o.GetType().FullName, ex);
            }

            outputStruct.Struct_ = inlineStruct;

            return outputStruct;
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
                case VariableValue.ValueOneofCase.UtcTimestamp:
                    return VariableType.Timestamp;
                case VariableValue.ValueOneofCase.WfRunId:
                    return VariableType.WfRunId;
                case VariableValue.ValueOneofCase.Array:
                    throw new NotSupportedException(
                        "Native LH ARRAY does not map to a single VariableType. Use TypeDefinition.InlineArrayDef instead.");
                case VariableValue.ValueOneofCase.JsonObj:
                    return VariableType.JsonObj;
                case VariableValue.ValueOneofCase.None:
                default:
                    return VariableType.JsonObj;
            }
        }

        internal static bool TryGetListElementType(Type type, out Type elementType)
        {
            if (type.IsGenericType && type.GetGenericTypeDefinition() == typeof(IList<>))
            {
                elementType = type.GetGenericArguments()[0];
                return true;
            }

            Type? listInterface = type.GetInterfaces()
                .FirstOrDefault(i => i.IsGenericType && i.GetGenericTypeDefinition() == typeof(IList<>));

            if (listInterface != null)
            {
                elementType = listInterface.GetGenericArguments()[0];
                return true;
            }

            elementType = null!;
            return false;
        }

        /// <summary>
        /// Maps a .NET <see cref="Type"/> to a workflow <see cref="ReturnType"/>.
        /// </summary>
        /// <param name="type">The .NET type to map.</param>
        /// <returns>The corresponding workflow <see cref="ReturnType"/>.</returns>
        /// <exception cref="ArgumentException">Thrown if the type is not supported.</exception>
        public static ReturnType DotNetTypeToReturnType(Type? type)
        {
            if (type == null)
            {
                throw new ArgumentNullException(nameof(type),"Type cannot be null.");
            }
            var typeDef = LHClassType.FromType(type).GetTypeDefinition();
            return new ReturnType { ReturnType_ = typeDef };
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
                case TaskStatus.TaskOutputSerdeError:
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

        private static Common.Proto.Array ToNativeArray(System.Array array, Type? elementType)
        {
            var output = new Common.Proto.Array();
            foreach (object? item in array)
            {
                output.Items.Add(SerializeArrayElement(item, elementType));
            }

            return output;
        }

        private static Common.Proto.Array ToNativeArray(IList list, Type? elementType)
        {
            var output = new Common.Proto.Array();
            foreach (object? item in list)
            {
                output.Items.Add(SerializeArrayElement(item, elementType));
            }

            return output;
        }

        /// <summary>
        /// Serializes a single array element using the declared element type when available,
        /// mirroring the Java SDK's type-aware per-element serialization.
        /// </summary>
        private static VariableValue SerializeArrayElement(object? item, Type? elementType)
        {
            if (item == null)
            {
                return new VariableValue();
            }

            // If the element itself is a native array/list and we have a nested element type,
            // recurse so that multi-dimensional LH Arrays are handled correctly.
            if (elementType != null && elementType.IsArray && elementType != typeof(byte[]) && item is System.Array nestedArray)
            {
                return ObjectToVariableValueAsNativeArray(nestedArray, elementType);
            }

            return ObjectToVariableValue(item);
        }

        private static object DeserializeNativeArrayToObject(Common.Proto.Array arrayValue, Type targetType)
        {
            if (targetType == typeof(byte[]))
            {
                throw new LHSerdeException("Cannot deserialize native LH ARRAY into byte[]. Use BYTES instead.");
            }

            if (targetType.IsArray)
            {
                Type? elementType = targetType.GetElementType();
                if (elementType == null)
                {
                    throw new LHSerdeException($"Unable to resolve array element type for {targetType.FullName}.");
                }

                System.Array output = System.Array.CreateInstance(elementType, arrayValue.Items.Count);
                for (int i = 0; i < arrayValue.Items.Count; i++)
                {
                    output.SetValue(VariableValueToObject(arrayValue.Items[i], elementType), i);
                }

                return output;
            }

            if (TryGetListElementType(targetType, out Type listElementType))
            {
                Type concreteListType = typeof(List<>).MakeGenericType(listElementType);
                IList outputList = (IList)(Activator.CreateInstance(concreteListType)
                    ?? throw new LHSerdeException($"Could not instantiate {concreteListType.FullName}."));

                foreach (VariableValue item in arrayValue.Items)
                {
                    outputList.Add(VariableValueToObject(item, listElementType));
                }

                return outputList;
            }

            throw new LHSerdeException(
                $"Cannot deserialize native LH ARRAY into target type [{targetType.FullName}]. Expected array or IList<T>.");
        }
    }
}
