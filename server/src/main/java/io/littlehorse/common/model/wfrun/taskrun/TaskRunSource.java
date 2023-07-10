package io.littlehorse.common.model.wfrun.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.TaskRunSourcePb;
import io.littlehorse.sdk.common.proto.TaskRunSourcePb.TaskRunSourceCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRunSource extends LHSerializable<TaskRunSourcePb> {

    private TaskRunSourceCase type;
    private TaskNodeReference taskNode;
    private UserTaskTriggerReference userTaskTrigger;

    public TaskRunSource() {}

    public TaskRunSource(TaskRunSubSource<?> source) {
        if (source.getClass().equals(TaskNodeReference.class)) {
            this.type = TaskRunSourceCase.TASK_NODE;
            this.taskNode = (TaskNodeReference) source;
        } else if (source.getClass().equals(UserTaskTriggerReference.class)) {
            this.type = TaskRunSourceCase.USER_TASK_TRIGGER;
            this.userTaskTrigger = (UserTaskTriggerReference) source;
        } else {
            throw new IllegalArgumentException(
                "Unexpected TaskRunSubSourceClass: " + source.getClass()
            );
        }
    }

    public Class<TaskRunSourcePb> getProtoBaseClass() {
        return TaskRunSourcePb.class;
    }

    public void initFrom(Message proto) {
        TaskRunSourcePb p = (TaskRunSourcePb) proto;
        type = p.getTaskRunSourceCase();
        switch (type) {
            case TASK_NODE:
                taskNode =
                    LHSerializable.fromProto(
                        p.getTaskNode(),
                        TaskNodeReference.class
                    );
                break;
            case USER_TASK_TRIGGER:
                userTaskTrigger =
                    LHSerializable.fromProto(
                        p.getUserTaskTrigger(),
                        UserTaskTriggerReference.class
                    );
                break;
            case TASKRUNSOURCE_NOT_SET:
            // Not really possible. Maybe throw error?
        }
    }

    public TaskRunSourcePb.Builder toProto() {
        TaskRunSourcePb.Builder out = TaskRunSourcePb.newBuilder();
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
