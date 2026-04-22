package io.littlehorse.server.streams.storeinternals.index;

import io.littlehorse.common.proto.TagStorageType;

public class IndexedField {
    private String key;
    private Object value;
    private TagStorageType tagStorageType;

    public String getKey() {
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }

    public TagStorageType getTagStorageType() {
        return this.tagStorageType;
    }

    public IndexedField(final String key, final Object value, final TagStorageType tagStorageType) {
        this.key = key;
        this.value = value;
        this.tagStorageType = tagStorageType;
    }
}
