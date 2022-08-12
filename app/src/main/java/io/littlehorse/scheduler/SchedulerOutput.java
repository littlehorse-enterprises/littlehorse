package io.littlehorse.scheduler;

import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.observability.ObservabilityEvents;
import io.littlehorse.scheduler.model.SchedulerTimer;

public class SchedulerOutput {
    public TaskScheduleRequest request;
    public ObservabilityEvents observabilityEvents;
    public SchedulerTimer timer;
}
