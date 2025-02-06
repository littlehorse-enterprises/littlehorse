package io.littlehorse.sdk.common.util;

public class JsonResult {
    public enum JsonType {
        STRING,
        ARRAY,
        OBJECT
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
}
