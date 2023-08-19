package io.littlehorse.common.model.wfrun.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.TaskAttemptModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TaskRunSubSource<T extends Message> extends LHSerializable<T> {

    /*
     * This method is called by the TaskRun when the TaskRun has succeeded and the
     * Workflow can continue.
     */
    public abstract void onCompleted(TaskAttemptModel succeededAttempt, LHDAO dao);

    /*
     * This method is called by the TaskRun object when it's determined that the
     * TaskRun has failed.
     */
    public abstract void onFailed(TaskAttemptModel lastFailure, LHDAO dao);
}
