package io.littlehorse.common.model.wfrun;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.TaskRunPb;
import io.littlehorse.common.proto.TaskRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class TaskRun extends GETable<TaskRunPb> {

  public String wfRunId;
  public int threadRunNumber;
  public int position;

  public int number;
  public int attemptNumber;
  public LHStatusPb status;
  public byte[] output;
  public byte[] logOutput;

  public Date scheduleTime;
  public Date startTime;
  public Date endTime;

  public String wfSpecId;
  public String wfSpecName;
  public String threadSpecName;
  public String nodeName;
  public String taskDefId;

  public TaskResultCodePb resultCode;

  public String getObjectId() {
    return TaskRun.getStoreKey(wfRunId, threadRunNumber, position);
  }

  public static String getStoreKey(String wfRunId, int threadNum, int position) {
    return wfRunId + "-" + threadNum + "-" + position;
  }

  public String getPartitionKey() {
    return wfRunId;
  }

  public Class<TaskRunPb> getProtoBaseClass() {
    return TaskRunPb.class;
  }

  public Date getCreatedAt() {
    return scheduleTime;
  }

  public void initFrom(MessageOrBuilder p) {
    TaskRunPbOrBuilder proto = (TaskRunPbOrBuilder) p;
    wfRunId = proto.getWfRunId();
    threadRunNumber = proto.getThreadRunNumber();
    position = proto.getPosition();

    number = proto.getNumber();
    attemptNumber = proto.getAttemptNumber();
    if (proto.hasOutput()) output = proto.getOutput().toByteArray();
    if (proto.hasLogOutput()) output = proto.getLogOutput().toByteArray();

    scheduleTime = LHUtil.fromProtoTs(proto.getScheduleTime());
    if (proto.hasStartTime()) {
      startTime = LHUtil.fromProtoTs(proto.getStartTime());
    }
    if (proto.hasEndTime()) {
      endTime = LHUtil.fromProtoTs(proto.getEndTime());
    }

    wfSpecId = proto.getWfSpecId();
    threadSpecName = proto.getThreadSpecName();
    nodeName = proto.getNodeName();
    taskDefId = proto.getTaskDefId();
    status = proto.getStatus();

    if (proto.hasResultCode()) resultCode = proto.getResultCode();
  }

  public TaskRunPb.Builder toProto() {
    TaskRunPb.Builder out = TaskRunPb
      .newBuilder()
      .setWfRunId(wfRunId)
      .setThreadRunNumber(threadRunNumber)
      .setPosition(position)
      .setNumber(number)
      .setAttemptNumber(attemptNumber)
      .setStatus(status)
      .setScheduleTime(LHUtil.fromDate(scheduleTime))
      .setWfSpecId(wfSpecId)
      .setNodeName(nodeName)
      .setTaskDefId(taskDefId);

    if (output != null) out.setOutput(ByteString.copyFrom(output));
    if (logOutput != null) out.setLogOutput(ByteString.copyFrom(logOutput));

    if (startTime != null) out.setStartTime(LHUtil.fromDate(startTime));
    if (endTime != null) out.setEndTime(LHUtil.fromDate(endTime));

    if (resultCode != null) out.setResultCode(resultCode);

    return out;
  }

  public List<Tag> getTags() {
    return Arrays.asList(
      new Tag(
        this,
        Pair.of("taskDefId", taskDefId),
        Pair.of("status", status.toString())
      )
    );
  }
}
