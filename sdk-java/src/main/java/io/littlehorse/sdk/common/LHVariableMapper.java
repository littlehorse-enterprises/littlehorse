package io.littlehorse.sdk.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * Utility class to transform LittleHorse objects into Java objects
 */
public final class LHVariableMapper {

    private static Gson gson = new Gson();

    private LHVariableMapper() {
        // no instances for this class
    }

    /**
     * Converts a VariableValue to an Integer.
     *
     * This method enforces that the VariableValue contains an integer value.
     *
     * @param var The VariableValue to convert to an integer.
     * @return The integer value represented by the VariableValue.
     * @throws IllegalArgumentException If the VariableValue does not contain an integer.
     */
    public static int asInt(VariableValue var) {
        enforceType(var.getValueCase(), Integer.class);
        return (int) var.getInt();
    }

    /**
     * Converts a VariableValue to a Long.
     *
     * This method enforces that the VariableValue contains a long value.
     *
     * @param var The VariableValue to convert to a long.
     * @return The long value represented by the VariableValue.
     * @throws IllegalArgumentException If the VariableValue does not contain a long.
     */
    public static long asLong(VariableValue var) {
        enforceType(var.getValueCase(), Long.class);
        return var.getInt();
    }

    /**
     * Converts a VariableValue to a Double.
     *
     * This method enforces that the VariableValue contains a double value.
     *
     * @param var The VariableValue to convert to a double.
     * @return The double value represented by the VariableValue.
     * @throws IllegalArgumentException If the VariableValue does not contain a double.
     */
    public static double asDouble(VariableValue var) {
        enforceType(var.getValueCase(), Double.class);
        return var.getDouble();
    }

    /**
     * Converts a VariableValue to a Boolean.
     *
     * This method enforces that the VariableValue contains a boolean value.
     *
     * @param var The VariableValue to convert to a boolean.
     * @return The boolean value represented by the VariableValue.
     * @throws IllegalArgumentException If the VariableValue does not contain a boolean.
     */
    public static boolean asBoolean(VariableValue var) {
        enforceType(var.getValueCase(), Boolean.class);
        return var.getBool();
    }

    /**
     * Converts a VariableValue to bytes.
     *
     * This method enforces that the VariableValue contains bytes in the value.
     *
     * @param var The VariableValue to convert to a bytes.
     * @return The bytes value represented by the VariableValue.
     * @throws IllegalArgumentException If the VariableValue does not contain bytes.
     */
    public static byte[] asBytes(VariableValue var) {
        enforceType(var.getValueCase(), Byte.class);
        return var.getBytes().toByteArray();
    }

    /**
     * Converts a VariableValue to String.
     *
     * This method enforces that the VariableValue contains a String value.
     *
     * @param var The VariableValue to convert to a bytes.
     * @return The bytes value represented by the VariableValue.
     * @throws IllegalArgumentException If the VariableValue does not contain bytes.
     */
    public static String asString(VariableValue var) {
        enforceType(var.getValueCase(), String.class);
        return var.getStr();
    }

    /**
     * Converts a VariableValue to a deserialized Json object.
     * This method enforces that the VariableValue contains a Json object value.
     *
     * @param var The VariableValue to convert to a Json object.
     * @return The deserialized Json object value represented by the VariableValue.
     * @throws IllegalArgumentException If the VariableValue does not contain Json object.
     */
    public static <T> T as(VariableValue var, Class<T> clazz) {
        return gson.fromJson(var.getJsonObj(), clazz);
    }

    /**
     * Converts a VariableValue to a Collection of deserialized Json objects.
     * This method enforces that the VariableValue contains a Json array value.
     *
     * @param var The VariableValue to convert.
     * @return The collection of deserialized Json objects value represented by the VariableValue.
     * @throws IllegalArgumentException If the VariableValue does not contain Json array.
     */
    public static <T> Collection<T> asList(VariableValue var, Class<T> clazz) {
        enforceType(var.getValueCase(), Collection.class);
        Type typeOfT = TypeToken.getParameterized(Collection.class, clazz).getType();
        return new Gson().fromJson(var.getJsonArr(), typeOfT);
    }

    private static void enforceType(VariableValue.ValueCase valueCase, Class<?> targetType) {
        boolean isCompatible = false;
        switch (valueCase) {
            case INT:
                isCompatible = targetType.equals(Integer.class) || targetType.equals(Long.class);
                break;
            case STR:
                isCompatible = targetType.equals(String.class);
                break;
            case BOOL:
                isCompatible = targetType.equals(Boolean.class);
                break;
            case BYTES:
                isCompatible = targetType.equals(Byte.class);
                break;
            case DOUBLE:
                isCompatible = targetType.equals(Double.class);
                break;
            case JSON_ARR:
                isCompatible = targetType.equals(Collection.class);
                break;
            case JSON_OBJ:
            case VALUE_NOT_SET:
            default:
        }
        if (!isCompatible) {
            throw new IllegalArgumentException(String.format(
                    "Not possible to convert variable type %s to %s", valueCase.name(), targetType.getSimpleName()));
        }
    }
}
