package io.littlehorse.sdk.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.littlehorse.sdk.common.proto.VariableValue;

import java.util.List;

public final class LHVariableMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LHVariableMapper() {
        // no instances for this class
    }

    public static int asInt(VariableValue var) {
        return (int) var.getInt();
    }

    public static long asLong(VariableValue var){
        return var.getInt();
    }

    public static double asDouble(VariableValue var) {
        return var.getDouble();
    }

    public static boolean asBoolean(VariableValue var) {
        return var.getBool();
    }
    public static byte[] asBytes(VariableValue var) {
        return var.getBytes().toByteArray();
    }

    public static String asString(VariableValue var) {
        return var.getStr();
    }

    public static <T> List<T> asList(VariableValue var, Class<T> clazz) {
        try {
            return MAPPER.readValue(var.getJsonArr(), TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
        } catch(JsonProcessingException exn) {
            throw new RuntimeException(exn);
        }
    }

}
