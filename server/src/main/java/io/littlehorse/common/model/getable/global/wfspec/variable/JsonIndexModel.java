package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Objects;
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
        JsonIndex.Builder out = JsonIndex.newBuilder().setPath(path).setIndexType(indexType);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        JsonIndex p = (JsonIndex) proto;
        path = p.getPath();
        indexType = p.getIndexType();
    }

    public TagStorageType getTagStorageType() {
        return indexType == IndexType.LOCAL_INDEX ? TagStorageType.LOCAL : TagStorageType.REMOTE;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;

        if (!(other instanceof JsonIndexModel)) {
            return false;
        }

        JsonIndexModel o = (JsonIndexModel) other;
        return Objects.equals(path, o.getPath()) && Objects.equals(indexType, o.getIndexType());
    }
}
