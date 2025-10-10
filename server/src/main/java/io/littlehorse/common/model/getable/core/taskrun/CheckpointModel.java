package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.CheckpointIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.Checkpoint;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckpointModel extends CoreGetable<Checkpoint> implements CoreOutputTopicGetable<Checkpoint> {

    private CheckpointIdModel id;
    private VariableValueModel value;
    private String logs;
    private Date createdAt;

    public CheckpointModel() {
        createdAt = new Date();
    }

    @Override
    public Class<Checkpoint> getProtoBaseClass() {
        return Checkpoint.class;
    }

    @Override
    public Checkpoint.Builder toProto() {
        Checkpoint.Builder out = Checkpoint.newBuilder()
                .setId(id.toProto())
                .setValue(value.toProto())
                .setCreatedAt(LHLibUtil.fromDate(createdAt));
        if (logs != null) {
            out.setLogs(logs);
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        Checkpoint p = (Checkpoint) proto;
        id = LHSerializable.fromProto(p.getId(), CheckpointIdModel.class, ignored);
        value = VariableValueModel.fromProto(p.getValue(), ignored);
        createdAt = LHLibUtil.fromProtoTs(p.getCreatedAt());
        if (p.hasLogs()) logs = p.getLogs();
    }

    @Override
    public CheckpointIdModel getObjectId() {
        return id;
    }

    @Override
    public List<GetableIndex<?>> getIndexConfigurations() {
        // No need to search checkpoints.
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String asdf, Optional<TagStorageType> ignored) {
        return List.of();
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }
}
