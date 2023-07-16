package io.littlehorse.common.model.meta;

import io.littlehorse.sdk.common.proto.IndexTypePb;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JsonIndex {

    private String path;
    private IndexTypePb indexTypePb;
}
