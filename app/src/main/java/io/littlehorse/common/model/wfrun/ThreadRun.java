package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskResultEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.Edge;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.TaskResultOe;
import io.littlehorse.common.model.observability.TaskScheduledOe;
import io.littlehorse.common.model.observability.TaskStartOe;
import io.littlehorse.common.model.observability.ThreadStatusChangeOe;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.NodePb.NodeCase;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.proto.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

// TODO: I don't think this should be GETable. Maybe just LHSerializable.
public class ThreadRun extends LHSerializable<ThreadRunPb> {

  public String wfRunId;
  public int number;

  public LHStatusPb status;
  public String wfSpecId;
  public String threadSpecName;
  public int numSteps;

  public NodeRunState currentNodeRun;

  public Date startTime;
  public Date endTime;

  public ThreadRun() {
    variables = new HashMap<>();
  }

  public void initFrom(MessageOrBuilder p) {
    ThreadRunPb proto = (ThreadRunPb) p;
    wfRunId = proto.getWfRunId();
    number = proto.getNumber();
    status = proto.getStatus();
    wfSpecId = proto.getWfSpecId();
    threadSpecName = proto.getThreadSpecName();
    numSteps = proto.getNumSteps();
    startTime = LHUtil.fromProtoTs(proto.getStartTime());
    if (proto.hasEndTime()) {
      endTime = LHUtil.fromProtoTs(proto.getEndTime());
    }
    if (proto.hasCurrentNodeRun()) {
      currentNodeRun = NodeRunState.fromProto(proto.getCurrentNodeRun());
      currentNodeRun.threadRun = this;
    }
  }

  public ThreadRunPb.Builder toProto() {
    ThreadRunPb.Builder out = ThreadRunPb
      .newBuilder()
      .setWfRunId(wfRunId)
      .setNumber(number)
      .setStatus(status)
      .setWfSpecId(wfSpecId)
      .setThreadSpecName(threadSpecName)
      .setNumSteps(numSteps)
      .setStartTime(LHUtil.fromDate(startTime));

    if (endTime != null) {
      out.setEndTime(LHUtil.fromDate(endTime));
    }

    if (currentNodeRun != null) {
      out.setCurrentNodeRun(currentNodeRun.toProto());
    }
    return out;
  }

  public static ThreadRun fromProto(MessageOrBuilder p) {
    ThreadRun out = new ThreadRun();
    out.initFrom(p);
    return out;
  }

  public Class<ThreadRunPb> getProtoBaseClass() {
    return ThreadRunPb.class;
  }

  public List<Tag> getIndexEntries() {
    return new ArrayList<>();
  }

  // For Scheduler
  @JsonIgnore
  public WfRun wfRun;

  @JsonIgnore
  public Map<String, Variable> variables;

  @JsonIgnore
  private ThreadSpec threadSpec;

  @JsonIgnore
  private Variable getVariable(
    String varName,
    ReadOnlyKeyValueStore<String, Variable> store
  ) {
    Variable out = variables.get(varName);
    if (out == null) {
      out = store.get(varName);
      variables.put(varName, out);
    }
    return out;
  }

  @JsonIgnore
  public ThreadSpec getThreadSpec() {
    if (threadSpec == null) {
      threadSpec = wfRun.wfSpec.threadSpecs.get(threadSpecName);
    }
    return threadSpec;
  }

  @JsonIgnore
  public Node getCurrentNode() {
    if (currentNodeRun == null) {
      return getThreadSpec().nodes.get(getThreadSpec().entrypointNodeName);
    } else {
      return getThreadSpec().nodes.get(currentNodeRun.nodeName);
    }
  }

  @JsonIgnore
  public void advance(Date eventTime) {
    if (status != LHStatusPb.RUNNING) {
      if (status == LHStatusPb.HALTED) {
        // Note, now that we have timers as actual events in the `WFRun_Event` log, this
        // isn't a Panic-able error. Totally valid. Once we actually do something with the
        // HALTED state, we need to fix this a bit.
        throw new RuntimeException("Tried to advance HALTED thread");
      }
      if (status == LHStatusPb.HALTING) {
        status = LHStatusPb.HALTED;
      }
      if (status == LHStatusPb.COMPLETED || status == LHStatusPb.ERROR) {
        if (status == LHStatusPb.ERROR) LHUtil.log(
          "YIKERZ",
          wfRun.id,
          currentNodeRun.position,
          eventTime.getTime()
        );
        return;
      }
      return;
    }

    Node curNode = getCurrentNode();

    if (currentNodeRun == null) {
      // activate entrypoint node
      advanceFrom(curNode);
    } else if (currentNodeRun.status == LHStatusPb.COMPLETED) {
      // activate next node
      advanceFrom(curNode);
    } else if (currentNodeRun.status == LHStatusPb.ERROR) {
      // determine whether to retry or fail
      if (shouldRetry(curNode, currentNodeRun)) {
        scheduleRetry(curNode, currentNodeRun);
      } else {
        LHUtil.log(
          "Timing out",
          wfRun.id,
          currentNodeRun.position,
          eventTime.getTime()
        );
        setStatus(LHStatusPb.ERROR);
      }
    } else if (currentNodeRun.status == LHStatusPb.RUNNING) {
      // Nothing to do, just wait for next event to come in.
    } else if (currentNodeRun.status == LHStatusPb.STARTING) {
      // Nothing to do.
      // This is possible if we have scheduled a retry due to timeout and then an old
      // event just came in, or (to be implemented) if an external event comes in, or if
      // another thread notifies this thread that it's no longer blocked, etc.
    } else {
      throw new RuntimeException(
        "Unexpected state for noderun: " + currentNodeRun.status
      );
    }
  }

  private boolean shouldRetry(Node curNode, NodeRunState currNodeRun) {
    if (curNode.type != NodeCase.TASK) return false;

    return currNodeRun.attemptNumber < curNode.taskNode.retries;
  }

  private void scheduleRetry(Node curNode, NodeRunState curNodeRun) {
    scheduleTaskNode(curNode, curNodeRun.attemptNumber + 1);
  }

  private void advanceFrom(Node curNode) {
    Node nextNode = null;
    for (Edge e : curNode.outgoingEdges) {
      if (evaluateEdge(e)) {
        nextNode = e.getSinkNode();
        break;
      }
    }
    if (nextNode == null) {
      throw new RuntimeException(
        "Not possible to have a node with zero activated edges"
      );
    }

    activateNode(nextNode);
  }

  private void activateNode(Node node) {
    switch (node.type) {
      case ENTRYPOINT:
        throw new RuntimeException("Not possible.");
      case TASK:
        scheduleTaskNode(node);
        break;
      case EXIT:
        setStatus(LHStatusPb.COMPLETED);
        break;
      case NODE_NOT_SET:
        throw new RuntimeException("Invalid nodetype.");
    }
  }

  // TODO: Do some conditional logic processing here.
  private boolean evaluateEdge(Edge e) {
    return true;
  }

  private void scheduleTaskNode(Node node) {
    scheduleTaskNode(node, 0);
  }

  private void scheduleTaskNode(Node node, int attemptNumber) {
    if (node.type != NodeCase.TASK) {
      throw new RuntimeException("Yikerz");
    }

    if (currentNodeRun == null) {
      currentNodeRun = new NodeRunState();
      currentNodeRun.threadRun = this;
      if (attemptNumber > 0) {
        throw new RuntimeException("Not possible.");
      }
    } else {
      // Regardless of whether retry or not, actual position in the list increases.
      currentNodeRun.position++;

      // If we're doing a retry, it's the same logical number, so don't increment.
      if (attemptNumber == 0) {
        currentNodeRun.number++;
      }
    }

    currentNodeRun.nodeName = node.name;
    currentNodeRun.attemptNumber = attemptNumber;
    currentNodeRun.status = LHStatusPb.STARTING;

    TaskScheduleRequest tsr = new TaskScheduleRequest();

    // TODO: Add a TaskDefProcessor.
    tsr.replyKafkaTopic = LHConstants.WF_RUN_EVENT_TOPIC;
    tsr.taskDefId = node.taskNode.taskDefName;
    tsr.taskDefName = node.taskNode.taskDefName;
    tsr.taskRunNumber = currentNodeRun.number;
    tsr.taskRunPosition = currentNodeRun.position;
    tsr.threadRunNumber = number;
    tsr.wfRunId = wfRun.id;
    tsr.wfSpecId = wfRun.wfSpecId;
    tsr.nodeName = node.name;

    wfRun.oEvents.add(new ObservabilityEvent(new TaskScheduledOe(tsr), new Date()));

    wfRun.tasksToSchedule.add(tsr);
  }

  public void setStatus(LHStatusPb newStatus) {
    status = newStatus;

    Date time = new Date();
    wfRun.oEvents.add(
      new ObservabilityEvent(new ThreadStatusChangeOe(number, status), time)
    );

    wfRun.handleThreadStatus(number, time, newStatus);
  }

  public void processStartedEvent(WfRunEvent we) {
    wfRun.oEvents.add(
      new ObservabilityEvent(
        new TaskStartOe(we.startedEvent, currentNodeRun.nodeName),
        we.time
      )
    );
    TaskStartedEvent se = we.startedEvent;

    if (currentNodeRun.position != se.taskRunPosition) {
      // Out-of-order event due to race conditions between task worker
      // transactional producer and regular producer
      return;
    }
    currentNodeRun.status = LHStatusPb.RUNNING;

    // set timer for TimeOut
    WfRunEvent timerEvt = new WfRunEvent();
    timerEvt.wfRunId = wfRun.id;
    timerEvt.wfSpecId = wfRun.wfSpecId;
    Node node = getCurrentNode();

    timerEvt.type = EventCase.TASK_RESULT;
    timerEvt.taskResult = new TaskResultEvent();
    timerEvt.taskResult.resultCode = TaskResultCodePb.TIMEOUT;
    timerEvt.taskResult.taskRunNumber = currentNodeRun.number;
    timerEvt.taskResult.taskRunPosition = currentNodeRun.position;
    timerEvt.taskResult.threadRunNumber = number;
    timerEvt.time =
      new Date(new Date().getTime() + (1000 * node.taskNode.timeoutSeconds));
    timerEvt.taskResult.time = timerEvt.time;

    wfRun.timersToSchedule.add(new LHTimer(timerEvt, timerEvt.time));
  }

  public void processCompletedEvent(WfRunEvent we) {
    wfRun.oEvents.add(
      new ObservabilityEvent(
        new TaskResultOe(we.taskResult, currentNodeRun.nodeName),
        we.time
      )
    );
    TaskResultEvent ce = we.taskResult;
    if (currentNodeRun.position > ce.taskRunPosition) {
      // TODO: Determine if this is theoretically impossible.
      // If it's impossible, throw exception to prevent silent bugs.

      // Update 8/25: I think this is legally possible eg when a task gets timed out but
      // then the event comes in later. Perhaps if the producer retry timeout on the task
      // worker is longer than the task timeout...

      // default delivery.timeout.ms is 2 minutes.
      // TODO: As an experiment, see what happens when we reduce that to something less than
      // the task timeout (which we have at 10 seconds). If our theory is correct, then
      // we shouldn't get any warnings for stale task timeouts.

      // Also of note is the default `request.timeout.ms` set to 30 seconds. Also greater than
      // our task timeout.
      return;
    }

    if (currentNodeRun.position < ce.taskRunPosition) {
      throw new RuntimeException("Caught a message from the future!");
    }

    if (currentNodeRun.number != ce.taskRunNumber) {
      // Out-of-order event due to race conditions between task worker
      // transactional producer and regular producer
      return;
    }

    switch (ce.resultCode) {
      case SUCCESS:
        currentNodeRun.status = LHStatusPb.COMPLETED;
        break;
      case TIMEOUT:
      case TASK_FAILURE:
        currentNodeRun.status = LHStatusPb.ERROR;
        break;
      case UNRECOGNIZED:
        throw new RuntimeException("Unrecognized TaskResultCode: " + ce.resultCode);
    }
  }
}
