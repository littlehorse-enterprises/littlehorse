package io.littlehorse.server.processors.util;

import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.observability.ObservabilityEvents;
import io.littlehorse.common.model.wfrun.LHTimer;

public class SchedulerOutput {

  public TaskScheduleRequest request;
  public ObservabilityEvents observabilityEvents;
  public LHTimer timer;
}
