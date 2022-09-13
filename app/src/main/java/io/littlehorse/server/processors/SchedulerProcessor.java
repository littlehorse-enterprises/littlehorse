package io.littlehorse.server.processors;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.scheduler.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.ServerTopology;
import io.littlehorse.server.model.scheduler.LHTimer;
import io.littlehorse.server.model.scheduler.WfRunState;
import io.littlehorse.server.model.scheduler.util.SchedulerOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class SchedulerProcessor
  implements Processor<String, WfRunEvent, String, SchedulerOutput> {

  private KeyValueStore<String, WfRunState> wfRunStore;
  private Map<String, WfSpec> wfSpecCache;
  private ProcessorContext<String, SchedulerOutput> context;
  private ReadOnlyKeyValueStore<String, WfSpec> wfSpecStore;

  public SchedulerProcessor(LHConfig config) {}

  @Override
  public void init(final ProcessorContext<String, SchedulerOutput> context) {
    wfRunStore = context.getStateStore(LHConstants.SCHED_WF_RUN_STORE_NAME);
    wfSpecStore =
      context.getStateStore(POSTable.getGlobalStoreName(WfSpec.class));

    this.context = context;
    this.wfSpecCache = new HashMap<>();
  }

  @Override
  public void process(final Record<String, WfRunEvent> record) {
    if (record.value() == null) {
      // Then it's a null event which is used simply to tick up the stream time for the
      // timer clearing method.
      return;
    }
    safeProcess(record.key(), record.timestamp(), record.value());
  }

  private void safeProcess(String key, long timestamp, WfRunEvent value) {
    try {
      processHelper(key, timestamp, value);
    } catch (Exception exn) {
      String wfRunId = key;
      WfRunState wfRun = wfRunStore.get(wfRunId);
      if (wfRun == null) {
        exn.printStackTrace();
        return;
      }
      try {
        exn.printStackTrace();
        wfRun.status = LHStatusPb.ERROR;
        LHUtil.log(
          "Bad WFRun, putting in ERROR. TODO: add a FailureMessage to WFRun"
        );
        wfRunStore.put(wfRunId, wfRun);
      } catch (Exception exn2) {
        exn2.printStackTrace();
      }
    }
  }

  private void processHelper(String key, long timestamp, WfRunEvent e) {
    WfSpec spec = getWfSpec(e.wfSpecId);
    if (spec == null) {
      LHUtil.log("Couldn't find spec, TODO: DeadLetter Queue");
      return;
    }

    WfRunState wfRun = wfRunStore.get(key);

    List<TaskScheduleRequest> tasksToSchedule = new ArrayList<>();
    List<LHTimer> timersToSchedule = new ArrayList<>();

    if (e.type == EventCase.RUN_REQUEST) {
      if (wfRun != null) {
        LHUtil.log("Got a past run for id " + key + ", skipping");
        return;
      }
      wfRun = spec.startNewRun(e, tasksToSchedule, timersToSchedule);
    } else {
      wfRun.wfSpec = spec;
      wfRun.processEvent(e, tasksToSchedule, timersToSchedule);
    }

    // Schedule tasks
    for (TaskScheduleRequest r : tasksToSchedule) {
      SchedulerOutput taskOutput = new SchedulerOutput();
      taskOutput.request = r;
      context.forward(
        new Record<>(key, taskOutput, timestamp),
        ServerTopology.schedulerTaskSink
      );
    }

    for (LHTimer timer : timersToSchedule) {
      SchedulerOutput out = new SchedulerOutput();
      out.timer = timer;
      context.forward(
        new Record<>(key, out, timestamp),
        ServerTopology.newTimerSink
      );
    }

    // Forward the observability events
    SchedulerOutput oeOutput = new SchedulerOutput();
    oeOutput.observabilityEvents = wfRun.oEvents;
    context.forward(
      new Record<>(key, oeOutput, timestamp),
      ServerTopology.schedulerWfRunSink
    );

    // Save the WfRunState
    wfRunStore.put(key, wfRun);
  }

  private WfSpec getWfSpec(String id) {
    WfSpec out = wfSpecCache.get(id);
    if (out == null) {
      out = wfSpecStore.get(id);
    }
    return out;
  }
}
