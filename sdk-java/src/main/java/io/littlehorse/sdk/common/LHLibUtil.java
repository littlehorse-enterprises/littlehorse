package io.littlehorse.sdk.common;

import static com.google.protobuf.util.Timestamps.fromMillis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ToNumberPolicy;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import io.littlehorse.sdk.common.exception.LHJsonProcessingException;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskRunSourceOrBuilder;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.util.JsonResult;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructProperty;
import io.littlehorse.sdk.worker.LHStructDef;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class LHLibUtil {

    public static Date fromProtoTs(Timestamp proto) {
        if (proto == null) return null;
        Date out = Date.from(Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos()));

        if (out.getTime() == 0) {
            out = new Date();
        }

        return out;
    }

    public static Timestamp fromDate(Date date) {
        if (date == null) return null;
        return fromMillis(date.getTime());
    }

    public static Timestamp fromInstant(Instant instant) {
        if (instant == null) return null;
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static <T extends GeneratedMessage> T loadProto(byte[] data, Class<T> cls) throws LHSerdeException {
        try {
            return cls.cast(cls.getMethod("parseFrom", byte[].class).invoke(null, data));
        } catch (NoSuchMethodException | IllegalAccessException exn) {
            exn.printStackTrace();
            throw new RuntimeException("Passed in an invalid proto class. Not possible");
        } catch (InvocationTargetException exn) {
            exn.printStackTrace();
            throw new LHSerdeException(exn.getCause(), "Failed loading protobuf: " + exn.getMessage());
        }
    }

    public static String protoToJson(MessageOrBuilder thing) {
        try {
            return JsonFormat.printer().includingDefaultValueFields().print(thing);
        } catch (InvalidProtocolBufferException exn) {
            exn.printStackTrace();
            return null;
        }
    }

    public static byte[] toProto(GeneratedMessage.Builder<?> builder) {
        return builder.build().toByteArray();
    }

    public static final Gson LH_GSON = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                @Override
                public JsonElement serialize(Date value, Type type, JsonSerializationContext context) {
                    return new JsonPrimitive(value.toInstant().toString());
                }
            })
            .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                        throws JsonParseException {
                    try {
                        Instant instant = Instant.parse(json.getAsString());
                        return Date.from(instant);
                    } catch (DateTimeParseException ex) {
                        throw new JsonParseException("Invalid ISO-8601 date: " + json.getAsString(), ex);
                    }
                }
            })
            .registerTypeAdapter(Instant.class, new JsonSerializer<Instant>() {
                @Override
                public JsonElement serialize(Instant value, Type type, JsonSerializationContext context) {
                    return new JsonPrimitive(value.toString());
                }
            })
            .registerTypeAdapter(Instant.class, new JsonDeserializer<Instant>() {
                @Override
                public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                        throws JsonParseException {
                    try {
                        return Instant.parse(json.getAsString());
                    } catch (DateTimeParseException ex) {
                        throw new JsonParseException("Invalid ISO-8601 instant: " + json.getAsString(), ex);
                    }
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime value, Type type, JsonSerializationContext context) {
                    return new JsonPrimitive(value.toString());
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                        throws JsonParseException {
                    try {
                        String stringDate = json.getAsString();
                        return LocalDateTime.parse(stringDate);
                    } catch (DateTimeParseException ex) {
                        throw new JsonParseException("Invalid LocalDateTime: " + json.getAsString(), ex);
                    }
                }
            })
            .registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {
                @Override
                public JsonElement serialize(java.sql.Timestamp value, Type type, JsonSerializationContext context) {
                    if (value == null) return null;
                    return new JsonPrimitive(value.toInstant().toString());
                }
            })
            .registerTypeAdapter(java.sql.Timestamp.class, new JsonDeserializer<java.sql.Timestamp>() {
                @Override
                public java.sql.Timestamp deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                        throws JsonParseException {
                    try {
                        String stringDate = json.getAsString();
                        Instant instant = Instant.parse(stringDate);
                        return new java.sql.Timestamp(instant.toEpochMilli());
                    } catch (Exception ex) {
                        throw new JsonParseException("Invalid java.sql.Timestamp: " + json.getAsString(), ex);
                    }
                }
            })
            .registerTypeAdapter(Timestamp.class, new JsonSerializer<Timestamp>() {
                @Override
                public JsonElement serialize(Timestamp value, Type type, JsonSerializationContext context) {
                    Instant instant = Instant.ofEpochSecond(value.getSeconds(), value.getNanos());
                    return new JsonPrimitive(instant.toString());
                }
            })
            .registerTypeAdapter(Timestamp.class, new JsonDeserializer<Timestamp>() {
                @Override
                public Timestamp deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                        throws JsonParseException {
                    try {
                        Instant instant = Instant.parse(json.getAsString());
                        return Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build();
                    } catch (DateTimeParseException ex) {
                        throw new JsonParseException("Invalid ISO-8601 protobuf timestamp: " + json.getAsString(), ex);
                    }
                }
            })
            .create();

    public static JsonResult serializeToJson(Object o) {
        JsonElement jsonElement = LH_GSON.toJsonTree(o);
        JsonResult.JsonType jsonType = null;

        if (jsonElement.isJsonObject()) {
            jsonType = JsonResult.JsonType.OBJECT;
        } else if (jsonElement.isJsonArray()) {
            jsonType = JsonResult.JsonType.ARRAY;
        } else if (jsonElement.isJsonNull()) {
            jsonType = JsonResult.JsonType.NULL;
        } else if (jsonElement.isJsonPrimitive()) {
            jsonType = JsonResult.JsonType.PRIMITIVE;
        }

        return new JsonResult(jsonElement.toString(), jsonType);
    }

    public static <T extends Object> T deserializeFromjson(String json, Class<T> cls) throws LHJsonProcessingException {
        try {
            return LH_GSON.fromJson(json, cls);
        } catch (JsonSyntaxException exn) {
            throw new LHJsonProcessingException(exn.getMessage());
        }
    }

    public static WfRunId getWfRunId(TaskRunSourceOrBuilder taskRunSource) {
        switch (taskRunSource.getTaskRunSourceCase()) {
            case TASK_NODE:
                return taskRunSource.getTaskNode().getNodeRunId().getWfRunId();
            case USER_TASK_TRIGGER:
                return taskRunSource.getUserTaskTrigger().getNodeRunId().getWfRunId();
            case TASKRUNSOURCE_NOT_SET:
                // we end up returning null
        }
        return null;
    }

    public static WfRunId wfRunIdFromString(String id) {
        if (!id.contains("_")) {
            return WfRunId.newBuilder().setId(id).build();
        }
        String parentId = id.substring(0, id.lastIndexOf("_"));
        String childId = id.substring(id.lastIndexOf("_") + 1);
        return WfRunId.newBuilder()
                .setId(childId)
                .setParentWfRunId(LHLibUtil.wfRunIdFromString(parentId))
                .build();
    }

    public static ExternalEventDefId externalEventDefId(String name) {
        return ExternalEventDefId.newBuilder().setName(name).build();
    }

    public static TaskDefId taskDefId(String name) {
        return TaskDefId.newBuilder().setName(name).build();
    }

    public static String wfRunIdToString(WfRunId id) {
        StringBuilder out = new StringBuilder();
        if (id.hasParentWfRunId()) {
            out.append(wfRunIdToString(id.getParentWfRunId()));
            out.append("_");
        }
        out.append(id.getId());
        return out.toString();
    }

    public static String taskRunIdToString(TaskRunId taskRunId) {
        return wfRunIdToString(taskRunId.getWfRunId()) + "/" + taskRunId.getTaskGuid();
    }

    public static Object varValToObj(VariableValue val, Class<?> targetClazz) throws LHSerdeException {
        String jsonStr = null;

        switch (val.getValueCase()) {
            case INT:
                if (targetClazz == Long.class || targetClazz == long.class) {
                    return val.getInt();
                } else {
                    return (int) val.getInt();
                }
            case DOUBLE:
                if (targetClazz == Double.class || targetClazz == double.class) {
                    return val.getDouble();
                } else {
                    return (float) val.getDouble();
                }
            case STR:
                return val.getStr();
            case BYTES:
                return val.getBytes().toByteArray();
            case BOOL:
                return val.getBool();
            case JSON_ARR:
                jsonStr = val.getJsonArr();
                break;
            case JSON_OBJ:
                jsonStr = val.getJsonObj();
                break;
            case WF_RUN_ID:
                return val.getWfRunId();
            case STRUCT:
                Struct struct = val.getStruct();
                return deserializeStructToObject(struct, targetClazz);
            case UTC_TIMESTAMP:
                Timestamp timestamp = val.getUtcTimestamp();
                if (Timestamp.class.isAssignableFrom(targetClazz)) {
                    return timestamp;
                }
                if (targetClazz == Instant.class) {
                    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
                }
                if (targetClazz == LocalDateTime.class) {
                    Instant inst = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
                    return LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
                }
                if (java.sql.Timestamp.class.isAssignableFrom(targetClazz)) {
                    Instant inst = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
                    return new java.sql.Timestamp(inst.toEpochMilli());
                }
                return LHLibUtil.fromProtoTs(timestamp);
            case VALUE_NOT_SET:
                return null;
        }

        try {
            return LHLibUtil.deserializeFromjson(jsonStr, targetClazz);
        } catch (LHJsonProcessingException exn) {
            throw new LHSerdeException(exn, "Failed deserializing VariableValue from JSON");
        }
    }

    private static Object deserializeStructToObject(Struct struct, Class<?> clazz) throws LHSerdeException {
        LHClassType lhClassType = LHClassType.fromJavaClass(clazz);

        if (!(lhClassType instanceof LHStructDefType)) {
            throw new LHSerdeException("Failed deserializing Struct into class of type: " + lhClassType);
        }

        LHStructDefType structDefType = (LHStructDefType) lhClassType;

        try {
            Object structObject = structDefType.createInstance();

            List<LHStructProperty> structProperties = structDefType.getStructProperties();

            for (LHStructProperty property : structProperties) {
                String fieldName = property.getFieldName();
                if (!struct.getStruct().containsFields(fieldName)) {
                    throw new LHSerdeException(
                            null,
                            String.format(
                                    "Failed deserializing VariableValue into Struct because no such field [%s] exists on class [%s]",
                                    fieldName, clazz.getName()));
                }

                VariableValue fieldValue =
                        struct.getStruct().getFieldsMap().get(fieldName).getValue();

                property.setValueTo(structObject, fieldValue);
            }

            return structObject;
        } catch (InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | IntrospectionException
                | SecurityException e) {
            throw new LHSerdeException(e, "Failed deserializing Struct into Object");
        }
    }

    public static VariableValue objToVarVal(Object o) throws LHSerdeException {
        if (o instanceof VariableValue) return (VariableValue) o;

        VariableValue.Builder out = VariableValue.newBuilder();
        if (o == null) {
            // nothing to do
        } else if (Long.class.isAssignableFrom(o.getClass())) {
            out.setInt((Long) o);
        } else if (Integer.class.isAssignableFrom(o.getClass())) {
            out.setInt((Integer) o);
        } else if (Double.class.isAssignableFrom(o.getClass())) {
            out.setDouble((Double) o);
        } else if (Float.class.isAssignableFrom(o.getClass())) {
            out.setDouble((Float) o);
        } else if (o instanceof String) {
            out.setStr((String) o);
        } else if (o instanceof Boolean) {
            out.setBool((Boolean) o);
        } else if (o instanceof byte[]) {
            out.setBytes(ByteString.copyFrom((byte[]) o));
        } else if (o instanceof WfRunId) {
            out.setWfRunId((WfRunId) o);
        } else if (o.getClass().isAnnotationPresent(LHStructDef.class)) {
            out.setStruct(serializeToStruct(o));
        } else if (o instanceof Instant) {
            out.setUtcTimestamp(fromInstant((Instant) o));
        } else if (o instanceof LocalDateTime) {
            Instant inst = ((LocalDateTime) o).atZone(ZoneId.systemDefault()).toInstant();
            out.setUtcTimestamp(fromInstant(inst));
        } else if (o instanceof Date) {
            out.setUtcTimestamp(fromDate((Date) o));
        } else {
            // At this point, all we can do is try to make it a JSON type.
            JsonResult jsonResult = LHLibUtil.serializeToJson(o);

            switch (jsonResult.getType()) {
                case ARRAY:
                    out.setJsonArr(jsonResult.getJsonStr());
                    break;
                case OBJECT:
                    out.setJsonObj(jsonResult.getJsonStr());
                    break;
                case PRIMITIVE:
                    // Trims the quotes off the string
                    out.setStr(jsonResult
                            .getJsonStr()
                            .substring(1, jsonResult.getJsonStr().length() - 1));
                    break;
                case NULL:
                    break;
                default:
                    throw new LHSerdeException("Failed serializing object to Json: " + o.toString());
            }
        }

        return out.build();
    }

    /**
     * Serializes a Java Object to a Struct based on the object's properties that have public getter methods.
     * @param o is a Java object
     * @return a Struct where the fields mirror the object's properties
     */
    public static Struct serializeToStruct(Object o) {
        LHClassType lhClassType = LHClassType.fromJavaClass(o.getClass());

        if (!(lhClassType instanceof LHStructDefType))
            throw new IllegalStateException("Cannot serialize given object to Struct");

        LHStructDefType structDefType = (LHStructDefType) lhClassType;

        Struct.Builder outputStruct = Struct.newBuilder();

        outputStruct.setStructDefId(structDefType.getStructDefId());

        InlineStruct.Builder inlineStruct = InlineStruct.newBuilder();

        try {
            List<LHStructProperty> lhStructProperties = structDefType.getStructProperties();

            for (LHStructProperty property : lhStructProperties) {
                VariableValue fieldValue = property.getValueFrom(o);

                StructField structField =
                        StructField.newBuilder().setValue(fieldValue).build();

                inlineStruct.putFields(property.getFieldName(), structField);
            }
        } catch (Exception e) {
            throw new LHSerdeException(
                    e, "Failed serializing object to Struct: " + o.getClass().getName());
        }

        outputStruct.setStruct(inlineStruct);

        return outputStruct.build();
    }

    /**
     * Converts a ValueCase (from the VariableValue.value oneof field) to a
     * VariableType Enum.
     *
     * @param valueCase is the ValueCase from the VariableValue.
     * @return the corresponding VariableType.
     */
    public static VariableType fromValueCase(ValueCase valueCase) {
        switch (valueCase) {
            case STR:
                return VariableType.STR;
            case BYTES:
                return VariableType.BYTES;
            case INT:
                return VariableType.INT;
            case DOUBLE:
                return VariableType.DOUBLE;
            case JSON_ARR:
                return VariableType.JSON_ARR;
            case JSON_OBJ:
                return VariableType.JSON_OBJ;
            case BOOL:
                return VariableType.BOOL;
            case WF_RUN_ID:
                return VariableType.WF_RUN_ID;
            case UTC_TIMESTAMP:
                return VariableType.TIMESTAMP;
            case VALUE_NOT_SET:
            default:
                return null;
        }
    }

    public static boolean isINT(Class<?> cls) {
        return (Integer.class.isAssignableFrom(cls)
                || Long.class.isAssignableFrom(cls)
                || int.class.isAssignableFrom(cls)
                || long.class.isAssignableFrom(cls));
    }

    public static boolean isDOUBLE(Class<?> cls) {
        return (Double.class.isAssignableFrom(cls)
                || Float.class.isAssignableFrom(cls)
                || double.class.isAssignableFrom(cls)
                || float.class.isAssignableFrom(cls));
    }

    public static boolean isSTR(Class<?> cls) {
        return String.class.isAssignableFrom(cls);
    }

    public static boolean isBOOL(Class<?> cls) {
        return (Boolean.class.isAssignableFrom(cls) || boolean.class.isAssignableFrom(cls));
    }

    public static boolean isBYTES(Class<?> cls) {
        return byte[].class.isAssignableFrom(cls);
    }

    public static boolean isJSON_ARR(Class<?> cls) {
        return List.class.isAssignableFrom(cls) || cls.isArray();
    }

    public static boolean isWfRunId(Class<?> cls) {
        return WfRunId.class.isAssignableFrom(cls);
    }

    public static boolean isTIMESTAMP(Class<?> cls) {
        return Instant.class.isAssignableFrom(cls)
                || Date.class.isAssignableFrom(cls)
                || Timestamp.class.isAssignableFrom(cls)
                || LocalDateTime.class.isAssignableFrom(cls);
    }

    public static VariableType javaClassToLHVarType(Class<?> cls) {
        if (isINT(cls)) return VariableType.INT;

        if (isDOUBLE(cls)) return VariableType.DOUBLE;

        if (isSTR(cls)) return VariableType.STR;

        if (isBOOL(cls)) return VariableType.BOOL;

        if (isBYTES(cls)) return VariableType.BYTES;

        if (isJSON_ARR(cls)) return VariableType.JSON_ARR;

        if (isWfRunId(cls)) return VariableType.WF_RUN_ID;

        if (isTIMESTAMP(cls)) return VariableType.TIMESTAMP;

        return VariableType.JSON_OBJ;
    }

    public static boolean isJavaClassLHPrimitive(Class<?> clazz) {
        if (clazz.isPrimitive()) return true;
        if (clazz.equals(Short.class)) return true;
        if (clazz.equals(Integer.class)) return true;
        if (clazz.equals(Boolean.class)) return true;
        if (clazz.equals(Long.class)) return true;
        if (clazz.equals(Float.class)) return true;
        if (clazz.equals(Double.class)) return true;
        if (clazz.equals(String.class)) return true;
        if (clazz.equals(WfRunId.class)) return true;
        if (clazz.equals(Byte[].class)) return true;
        if (clazz.equals(byte[].class)) return true;

        return false;
    }

    public static boolean areVariableValuesEqual(VariableValue a, VariableValue b) {
        if (a.getValueCase() != b.getValueCase()) return false;

        switch (a.getValueCase()) {
            case INT:
                return a.getInt() == b.getInt();
            case DOUBLE:
                return a.getDouble() == b.getDouble();
            case STR:
                return a.getStr().equals(b.getStr());
            case BOOL:
                return a.getBool() == b.getBool();
            case JSON_ARR:
                return a.getJsonArr().equals(b.getJsonArr());
            case JSON_OBJ:
                return a.getJsonObj().equals(b.getJsonObj());
            case BYTES:
                return a.getBytes().equals(b.getBytes());
            case WF_RUN_ID:
                return a.getWfRunId().equals(b.getWfRunId());
            case UTC_TIMESTAMP:
                return a.getUtcTimestamp().equals(b.getUtcTimestamp());
            case VALUE_NOT_SET:
                return true;
            default:
                break;
        }
        throw new IllegalStateException("Not possible to get here");
    }
}
