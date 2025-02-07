package io.littlehorse.sdk.common.util;

public class JsonResult {
    public enum JsonType {
        /**
         * Represents a `com.google.gson.JsonPrimitive`
         */
        PRIMITIVE,
        /**
         * Represents a `com.google.gson.JsonArray`
         */
        ARRAY,
        /**
         * Represents a `com.google.gson.JsonObject`
         */
        OBJECT,
        /**
         * Represents a `com.google.gson.JsonNull`
         */
        NULL
    }

    private final String jsonStr;
    private final JsonType type;

    public JsonResult(String jsonStr, JsonType type) {
        this.jsonStr = jsonStr;
        this.type = type;
    }

    public String getJsonStr() {
        return this.jsonStr;
    }

    public JsonType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return String.format("JsonStr: %s, JsonType: %s", jsonStr, type);
    }
}
