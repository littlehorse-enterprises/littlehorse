package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.TaskRunSource;
import io.littlehorse.sdk.common.proto.TaskRunSource.TaskRunSourceCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRunSourceModel extends LHSerializable<TaskRunSource> {

    private TaskRunSourceCase type;
    private TaskNodeReferenceModel taskNode;
    private UserTaskTriggerReferenceModel userTaskTrigger;

    public TaskRunSourceModel() {}

    public TaskRunSourceModel(TaskRunSubSource<?> source) {
        if (source.getClass().equals(TaskNodeReferenceModel.class)) {
            this.type = TaskRunSourceCase.TASK_NODE;
            this.taskNode = (TaskNodeReferenceModel) source;
        } else if (source.getClass().equals(UserTaskTriggerReferenceModel.class)) {
            this.type = TaskRunSourceCase.USER_TASK_TRIGGER;
            this.userTaskTrigger = (UserTaskTriggerReferenceModel) source;
        } else {
            throw new IllegalArgumentException("Unexpected TaskRunSubSourceClass: " + source.getClass());
        }
    }

    public Class<TaskRunSource> getProtoBaseClass() {
        return TaskRunSource.class;
    }

    public void initFrom(Message proto) {
        TaskRunSource p = (TaskRunSource) proto;
        type = p.getTaskRunSourceCase();
        switch (type) {
            case TASK_NODE:
                taskNode = LHSerializable.fromProto(p.getTaskNode(), TaskNodeReferenceModel.class);
                break;
            case USER_TASK_TRIGGER:
                userTaskTrigger = LHSerializable.fromProto(p.getUserTaskTrigger(), UserTaskTriggerReferenceModel.class);
                break;
            case TASKRUNSOURCE_NOT_SET:
                // Not really possible. Maybe throw error?
        }
    }

    public TaskRunSource.Builder toProto() {
        TaskRunSource.Builder out = TaskRunSource.newBuilder();
        switch (type) {
            case TASK_NODE:
                out.setTaskNode(taskNode.toProto());
                break;
            case USER_TASK_TRIGGER:
                out.setUserTaskTrigger(userTaskTrigger.toProto());
                break;
            case TASKRUNSOURCE_NOT_SET:
                // Not really possible. Maybe throw error?
        }
        return out;
    }

    public TaskRunSubSource<?> getSubSource() {
        switch (type) {
            case TASK_NODE:
                return taskNode;
            case USER_TASK_TRIGGER:
                return userTaskTrigger;
            case TASKRUNSOURCE_NOT_SET:
                // Not really possible. Maybe throw error?
        }
        // This is impossible.
        return null;
    }
}
