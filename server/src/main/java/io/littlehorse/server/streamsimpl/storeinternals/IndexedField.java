package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.common.proto.TagStorageTypePb;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IndexedField {

    private String key;
    private Object value;
    private TagStorageTypePb tagStorageTypePb;
}
