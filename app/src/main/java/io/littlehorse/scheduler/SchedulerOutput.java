package io.littlehorse.scheduler;

import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.observability.ObservabilityEvents;

public class SchedulerOutput {
    public TaskScheduleRequest request;
    public ObservabilityEvents observabilityEvents;
}
