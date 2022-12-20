package io.littlehorse.testworker;

import io.littlehorse.common.proto.TaskScheduleRequestPb;
import io.littlehorse.common.proto.VariableValuePb;

public interface TaskFunc {
    public VariableValuePb execute(TaskScheduleRequestPb request);
}
