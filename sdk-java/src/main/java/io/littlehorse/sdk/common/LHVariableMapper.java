package io.littlehorse.sdk.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.Collection;
import java.util.List;

public final class LHVariableMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LHVariableMapper() {
        // no instances for this class
    }

    public static int asInt(VariableValue var) {
        enforceType(var.getValueCase(), Integer.class);
        return (int) var.getInt();
    }

    public static long asLong(VariableValue var) {
        enforceType(var.getValueCase(), Long.class);
        return var.getInt();
    }

    public static double asDouble(VariableValue var) {
        enforceType(var.getValueCase(), Double.class);
        return var.getDouble();
    }

    public static boolean asBoolean(VariableValue var) {
        enforceType(var.getValueCase(), Boolean.class);
        return var.getBool();
    }

    public static byte[] asBytes(VariableValue var) {
        enforceType(var.getValueCase(), Byte.class);
        return var.getBytes().toByteArray();
    }

    public static String asString(VariableValue var) {
        enforceType(var.getValueCase(), String.class);
        return var.getStr();
    }

    public static <T> T as(VariableValue var, Class<T> clazz) {
        try {
            return MAPPER.readValue(var.getJsonArr(), clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enforceType(VariableValue.ValueCase valueCase, Class<?> targetType) {
        List<Class<?>> compatibleTypes;
        switch (valueCase) {
            case INT:
                compatibleTypes = List.of(Integer.class, Long.class);
                break;
            case STR:
                compatibleTypes = List.of(String.class);
                break;
            case BOOL:
                compatibleTypes = List.of(Boolean.class);
                break;
            case BYTES:
                compatibleTypes = List.of(Byte.class);
                break;
            case DOUBLE:
                compatibleTypes = List.of(Double.class);
                break;
            case JSON_ARR:
                compatibleTypes = List.of(Collection.class);
                break;
            case JSON_OBJ:
            case VALUE_NOT_SET:
            default:
                compatibleTypes = List.of();
        }
        if (!compatibleTypes.contains(targetType)) {
            throw new IllegalArgumentException(String.format(
                    "Not possible to convert variable type %s to %s", valueCase.name(), targetType.getSimpleName()));
        }
    }

    public static <T> Collection<T> asList(VariableValue var, Class<T> clazz) {
        try {
            enforceType(var.getValueCase(), Collection.class);
            return MAPPER.readValue(
                    var.getJsonArr(), TypeFactory.defaultInstance().constructCollectionType(Collection.class, clazz));
        } catch (JsonProcessingException exn) {
            throw new RuntimeException(exn);
        }
    }
}
