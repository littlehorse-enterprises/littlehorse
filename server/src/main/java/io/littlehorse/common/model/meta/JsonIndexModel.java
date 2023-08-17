package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.JsonIndex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JsonIndexModel extends LHSerializable<JsonIndex> {

    private String path;
    private IndexType indexType;

    @Override
    public Class<JsonIndex> getProtoBaseClass() {
        return JsonIndex.class;
    }

    @Override
    public JsonIndex.Builder toProto() {
        JsonIndex.Builder out = JsonIndex
            .newBuilder()
            .setPath(path)
            .setIndexType(indexType);

        return out;
    }

    @Override
    public void initFrom(Message proto) {
        JsonIndex p = (JsonIndex) proto;
        path = p.getPath();
        indexType = p.getIndexType();
    }

    public TagStorageType getTagStorageType() {
        return indexType == IndexType.LOCAL_INDEX
            ? TagStorageType.LOCAL
            : TagStorageType.REMOTE;
    }
}
