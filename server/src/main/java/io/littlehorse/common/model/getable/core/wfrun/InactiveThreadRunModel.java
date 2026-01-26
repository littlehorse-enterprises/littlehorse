package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.getable.objectId.InactiveThreadRunIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InactiveThreadRun;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

public class InactiveThreadRunModel extends CoreGetable<InactiveThreadRun>
        implements CoreOutputTopicGetable<InactiveThreadRun> {

    private InactiveThreadRunIdModel id;

    @Getter
    private ThreadRunModel threadRun;

    public InactiveThreadRunModel() {}

    public InactiveThreadRunModel(ThreadRunModel threadRun) {
        this.threadRun = threadRun;
        this.id = new InactiveThreadRunIdModel(threadRun.getWfRun().getId(), threadRun.getNumber());
    }

    @Override
    public Date getCreatedAt() {
        return threadRun.getStartTime();
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public InactiveThreadRunIdModel getObjectId() {
        return id;
    }

    @Override
    public InactiveThreadRun.Builder toProto() {
        InactiveThreadRun.Builder out = InactiveThreadRun.newBuilder();
        out.setThreadRun(this.threadRun.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        InactiveThreadRun p = (InactiveThreadRun) proto;
        this.threadRun = ThreadRunModel.fromProto(p.getThreadRun(), context);
    }

    @Override
    public Class<InactiveThreadRun> getProtoBaseClass() {
        return InactiveThreadRun.class;
    }
}
