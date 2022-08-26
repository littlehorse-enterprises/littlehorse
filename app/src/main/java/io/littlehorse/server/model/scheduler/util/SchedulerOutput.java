package io.littlehorse.server.model.scheduler.util;

import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.observability.ObservabilityEvents;
import io.littlehorse.server.model.scheduler.SchedulerTimer;

public class SchedulerOutput {
    public TaskScheduleRequest request;
    public ObservabilityEvents observabilityEvents;
    public SchedulerTimer timer;
}
