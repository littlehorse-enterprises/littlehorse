package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.TaskQueueHint;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;

/**
 * See the description in the PR (TODO put PR number).
 *
 * Note that all ScheduledTask's get put in the same bucket, regardless of TaskDefId. What we
 * are now doing is ordering them by created time.
 */
public class TaskQueueHintModel extends Storeable<TaskQueueHint> {

    /**
     * All `ScheduledTaskModel`'s for every TaskDefId within a Tenant go in the same bucket.
     * And if all `ScheduledTaskModel`'s are processed sequentially by created time, we only
     * have to remember the processed time the most recently-processed ScheduledTaskModel of
     * any TaskDef, so we only need one hint.
     */
    public static final String TASK_QUEUE_HINT_KEY = "tqh";

    @Getter
    private Timestamp lastProcessedTimestamp;

    public TaskQueueHintModel() {}

    public TaskQueueHintModel(Date date) {
        this.lastProcessedTimestamp = LHUtil.fromDate(date);
    }

    @Override
    public Class<TaskQueueHint> getProtoBaseClass() {
        return TaskQueueHint.class;
    }

    @Override
    public TaskQueueHint.Builder toProto() {
        return TaskQueueHint.newBuilder().setLastProcessedTimestamp(lastProcessedTimestamp);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        TaskQueueHint p = (TaskQueueHint) proto;
        this.lastProcessedTimestamp = p.getLastProcessedTimestamp();
    }

    @Override
    public StoreableType getType() {
        return StoreableType.TASK_QUEUE_HINT;
    }

    @Override
    public String getStoreKey() {
        return TaskQueueHintModel.TASK_QUEUE_HINT_KEY;
    }

    public String getKeyToResumeFrom() {
        return ScheduledTaskModel.STORE_KEY_PREFIX_FOR_COMPATIBILITY
                + LHUtil.toLhDbFormat(LHUtil.fromProtoTs(lastProcessedTimestamp));
    }
}
