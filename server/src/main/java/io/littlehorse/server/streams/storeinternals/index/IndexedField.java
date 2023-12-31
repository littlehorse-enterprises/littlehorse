package io.littlehorse.server.streams.storeinternals.index;

import io.littlehorse.common.proto.TagStorageType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IndexedField {

    private String key;
    private Object value;
    private TagStorageType tagStorageType;
}
