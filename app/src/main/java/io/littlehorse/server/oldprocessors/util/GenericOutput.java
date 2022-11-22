package io.littlehorse.server.oldprocessors.util;

import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;

public class GenericOutput {

    public TaskScheduleRequest request;
    public LHTimer timer;
    public GETable<?> thingToTag;
}
