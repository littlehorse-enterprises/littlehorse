package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.sdk.common.proto.IndexTypePb;
import io.littlehorse.sdk.common.proto.JsonIndexPb;
import io.littlehorse.sdk.common.proto.VariableDefPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariableDef extends LHSerializable<VariableDefPb> {

    public VariableTypePb type;
    public String name;

    public ThreadSpec threadSpec;
    private TagStorageTypePb tagStorageTypePb;
    private List<JsonIndex> jsonIndices = new ArrayList<>();

    public Class<VariableDefPb> getProtoBaseClass() {
        return VariableDefPb.class;
    }

    public void initFrom(Message proto) {
        VariableDefPb p = (VariableDefPb) proto;
        type = p.getType();
        name = p.getName();
        jsonIndices =
            p
                .getJsonIndexesList()
                .stream()
                .map(jsonIndexPb ->
                    new JsonIndex(jsonIndexPb.getPath(), jsonIndexPb.getIndexType())
                )
                .toList();
        if (p.hasIndexType()) {
            if (p.getIndexType() == IndexTypePb.REMOTE_INDEX) {
                tagStorageTypePb = TagStorageTypePb.REMOTE;
            } else {
                tagStorageTypePb = TagStorageTypePb.LOCAL;
            }
        }
    }

    public VariableDefPb.Builder toProto() {
        IndexTypePb indexType = IndexTypePb.LOCAL_INDEX;
        if (tagStorageTypePb == TagStorageTypePb.REMOTE) {
            indexType = IndexTypePb.REMOTE_INDEX;
        }
        List<JsonIndexPb> jsonIndexPbs = jsonIndices
            .stream()
            .map(jsonIndex -> {
                return JsonIndexPb
                    .newBuilder()
                    .setPath(jsonIndex.getPath())
                    .setIndexType(jsonIndex.getIndexTypePb())
                    .build();
            })
            .toList();
        return VariableDefPb
            .newBuilder()
            .setType(type)
            .setName(name)
            .addAllJsonIndexes(jsonIndexPbs)
            .setIndexType(indexType);
    }

    public static VariableDef fromProto(VariableDefPb proto) {
        VariableDef o = new VariableDef();
        o.initFrom(proto);
        return o;
    }
}
