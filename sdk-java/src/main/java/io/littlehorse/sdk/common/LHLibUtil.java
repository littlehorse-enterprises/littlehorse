package io.littlehorse.sdk.common;

import static com.google.protobuf.util.Timestamps.fromMillis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskRunSource;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
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

    public static <T extends GeneratedMessageV3> T loadProto(byte[] data, Class<T> cls) throws LHSerdeError {
        try {
            return cls.cast(cls.getMethod("parseFrom", byte[].class).invoke(null, data));
        } catch (NoSuchMethodException | IllegalAccessException exn) {
            exn.printStackTrace();
            throw new RuntimeException("Passed in an invalid proto class. Not possible");
        } catch (InvocationTargetException exn) {
            exn.printStackTrace();
            throw new LHSerdeError(exn.getCause(), "Failed loading protobuf: " + exn.getMessage());
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

    public static byte[] toProto(GeneratedMessageV3.Builder<?> builder) {
        return builder.build().toByteArray();
    }

    private static ObjectMapper mapper = new ObjectMapper();

    public static String serializeToJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public static <T extends Object> T deserializeFromjson(String json, Class<T> cls) throws JsonProcessingException {
        return mapper.readValue(json, cls);
    }

    public static WfRunId getWfRunId(TaskRunSource taskRunSource) {
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

    public static WfRunId wfRunId(String id) {
        return WfRunId.newBuilder().setId(id).build();
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

    public static VariableValue objToVarVal(Object o) throws LHSerdeError {
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
        } else {
            // At this point, all we can do is try to make it a JSON type.
            String jsonStr;
            try {
                jsonStr = LHLibUtil.serializeToJson(o);
            } catch (JsonProcessingException exn) {
                exn.printStackTrace();
                throw new LHSerdeError(exn, "Failed deserializing json");
            }

            if (List.class.isAssignableFrom(o.getClass())) {
                out.setJsonArr(jsonStr);
            } else {
                out.setJsonObj(jsonStr);
            }
        }

        return out.build();
    }

    /**
     * Converts a ValueCase (from the VariableValue.value oneof field) to a VariableType Enum.
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
        return List.class.isAssignableFrom(cls);
    }

    public static VariableType javaClassToLHVarType(Class<?> cls) {
        if (isINT(cls)) return VariableType.INT;

        if (isDOUBLE(cls)) return VariableType.DOUBLE;

        if (isSTR(cls)) return VariableType.STR;

        if (isBOOL(cls)) return VariableType.BOOL;

        if (isBYTES(cls)) return VariableType.BYTES;

        if (isJSON_ARR(cls)) return VariableType.JSON_ARR;

        return VariableType.JSON_OBJ;
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
            case VALUE_NOT_SET:
                return true;
        }
        throw new IllegalStateException("Not possible to get here");
    }
}
