package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.sdk.common.proto.IndexTypePb;
import io.littlehorse.sdk.common.proto.JsonIndexPb;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JsonIndex extends LHSerializable<JsonIndexPb> {

    private String path;
    private IndexTypePb indexType;

    @Override
    public Class<JsonIndexPb> getProtoBaseClass() {
        return JsonIndexPb.class;
    }

    @Override
    public JsonIndexPb.Builder toProto() {
        JsonIndexPb.Builder out = JsonIndexPb
            .newBuilder()
            .setPath(path)
            .setIndexType(indexType);

        return out;
    }

    @Override
    public void initFrom(Message proto) {
        JsonIndexPb p = (JsonIndexPb) proto;
        path = p.getPath();
        indexType = p.getIndexType();
    }

    public TagStorageTypePb getTagStorageType() {
        return indexType == IndexTypePb.LOCAL_INDEX
            ? TagStorageTypePb.LOCAL
            : TagStorageTypePb.REMOTE;
    }
}
