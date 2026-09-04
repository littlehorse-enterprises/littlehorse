package io.littlehorse.sdk.conformance;

import com.google.gson.Gson;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * serde case definitions + the exam's `convert` answers. SerdeAreaMint
 * mints from these same definitions (rules.md S2: one source, so replayed
 * inputs are provably the inputs that produced the fixtures).
 */
public final class SerdeArea {

    /** Serde cases: id → {input type, input value} per rules.md S1. */
    private static final Map<String, String[]> CASES = new LinkedHashMap<>();

    private SerdeArea() {}

    private static void serdeCase(String id, String type, String value) {
        CASES.put(id, new String[] {type, value});
    }

    static {
        serdeCase("str-basic", "str", "hello");
        serdeCase("str-empty", "str", "");
        serdeCase("str-unicode", "str", "héllo → 世界");
        serdeCase("int-answer", "int", "42");
        serdeCase("int-zero", "int", "0");
        serdeCase("int-negative", "int", "-7");
        serdeCase("int-max-safe", "int", "9007199254740991");
        serdeCase("int-beyond-double", "int", "9007199254740993");
        serdeCase("double-half", "double", "1.5");
        serdeCase("double-negative", "double", "-0.25");
        serdeCase("double-whole", "double", "5");
        serdeCase("bool-true", "bool", "true");
        serdeCase("bool-false", "bool", "false");
        serdeCase("bytes-basic", "bytes", "AQID/w==");
        serdeCase("timestamp-millis", "timestamp", "1780000000123");
        serdeCase("json-obj-single", "json-obj", "{\"k\":\"v\"}");
        serdeCase("json-obj-empty", "json-obj", "{}");
        serdeCase("json-arr-single", "json-arr", "[\"v\"]");
        serdeCase("json-arr-empty", "json-arr", "[]");
        serdeCase("null-value", "null", null);
    }

    static Set<String> caseIds() {
        return CASES.keySet();
    }

    /** Maps a typed input (rules.md, S1) to the native value handed to the SDK. */
    private static Object nativeFromTyped(String type, String value) {
        return switch (type) {
            case "str" -> value;
            case "int" -> Long.parseLong(value);
            case "double" -> Double.parseDouble(value);
            case "bool" -> Boolean.parseBoolean(value);
            case "bytes" -> Base64.getDecoder().decode(value);
            case "timestamp" -> Instant.ofEpochMilli(Long.parseLong(value));
            case "json-obj" -> new Gson().fromJson(value, LinkedHashMap.class);
            case "json-arr" -> new Gson().fromJson(value, List.class);
            case "null" -> null;
            default -> throw new IllegalArgumentException("unknown input type: " + type);
        };
    }

    static String convert(String type, String value) throws Exception {
        VariableValue converted = LHLibUtil.objToVarVal(nativeFromTyped(type, value));
        return com.google.protobuf.util.JsonFormat.printer()
                .includingDefaultValueFields()
                .print(converted);
    }

    static Map<String, String[]> cases() {
        return CASES;
    }
}
