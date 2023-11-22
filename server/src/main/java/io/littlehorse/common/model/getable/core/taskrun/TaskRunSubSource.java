package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TaskRunSubSource<T extends Message> extends LHSerializable<T> {

    /*
     * This method is called by the TaskRun when the TaskRun has succeeded and the
     * Workflow can continue.
     */
    public abstract void onCompleted(TaskAttemptModel succeededAttempt);

    /*
     * This method is called by the TaskRun object when it's determined that the
     * TaskRun has failed.
     */
    public abstract void onFailed(TaskAttemptModel lastFailure);
}
