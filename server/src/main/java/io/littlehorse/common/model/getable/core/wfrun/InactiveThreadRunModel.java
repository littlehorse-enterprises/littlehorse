package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.getable.objectId.InactiveThreadRunIdModel;
import io.littlehorse.common.model.metadatacommand.OutputTopicConfigModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InactiveThreadRun;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
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

    @Getter
    private ArchivedThreadRunInfoModel archived;

    @Getter
    private QueuedThreadRunInfoModel queued;

    public InactiveThreadRunModel() {}

    public InactiveThreadRunModel(ThreadRunModel threadRun) {
        this(threadRun, new ArchivedThreadRunInfoModel(), null);
    }

    public InactiveThreadRunModel(ThreadRunModel threadRun, QueuedThreadRunInfoModel queued) {
        this(threadRun, null, queued);
    }

    private InactiveThreadRunModel(
            ThreadRunModel threadRun, ArchivedThreadRunInfoModel archived, QueuedThreadRunInfoModel queued) {
        this.threadRun = threadRun;
        this.archived = archived;
        this.queued = queued;
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

        if (queued != null) {
            out.setQueued(queued.toProto());
        } else {
            // An InactiveThreadRun with no explicit type defaults to archived, which also
            // keeps backward compatibility with data persisted before the oneof existed.
            out.setArchived(archived != null ? archived.toProto() : new ArchivedThreadRunInfoModel().toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        InactiveThreadRun p = (InactiveThreadRun) proto;
        this.threadRun = ThreadRunModel.fromProto(p.getThreadRun(), context);

        switch (p.getInactiveReasonCase()) {
            case ARCHIVED -> this.archived = ArchivedThreadRunInfoModel.fromProto(p.getArchived(), context);
            case QUEUED -> this.queued = QueuedThreadRunInfoModel.fromProto(p.getQueued(), context);
            // Data persisted before the oneof existed has no type set; treat it as archived.
            case INACTIVEREASON_NOT_SET -> this.archived = new ArchivedThreadRunInfoModel();
        }
    }

    @Override
    public Class<InactiveThreadRun> getProtoBaseClass() {
        return InactiveThreadRun.class;
    }

    @Override
    public boolean shouldProduceToOutputTopic(
            InactiveThreadRun previousValue,
            ReadOnlyMetadataManager metadataManager,
            ReadOnlyGetableManager getableManager,
            OutputTopicConfigModel config) {
        return false;
    }
}
