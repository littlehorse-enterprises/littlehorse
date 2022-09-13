package io.littlehorse.common.model.observability;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskResultEvent;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.observability.TaskResultOePb;

public class TaskResultOe extends LHSerializable<TaskResultOePb> {

  public int threadRunNumber;
  public int taskRunNumber;
  public int taskRunPosition;

  public TaskResultCodePb resultCode;
  public byte[] output;
  public byte[] logOutput;

  public String nodeName;

  public Class<TaskResultOePb> getProtoBaseClass() {
    return TaskResultOePb.class;
  }

  public TaskResultOePb.Builder toProto() {
    TaskResultOePb.Builder out = TaskResultOePb
      .newBuilder()
      .setThreadRunNumber(threadRunNumber)
      .setTaskRunNumber(taskRunNumber)
      .setTaskRunPosition(taskRunPosition)
      .setResultCode(resultCode)
      .setNodeName(nodeName);

    if (output != null) {
      out.setOutput(ByteString.copyFrom(output));
    }
    if (logOutput != null) {
      out.setLogOutput(ByteString.copyFrom(logOutput));
    }

    return out;
  }

  public void initFrom(MessageOrBuilder proto) {
    TaskResultOePb p = (TaskResultOePb) proto;
    threadRunNumber = p.getThreadRunNumber();
    taskRunNumber = p.getTaskRunNumber();
    taskRunPosition = p.getTaskRunPosition();
    resultCode = p.getResultCode();
    if (p.hasOutput()) output = p.getOutput().toByteArray();
    if (p.hasLogOutput()) logOutput = p.getLogOutput().toByteArray();
    nodeName = p.getNodeName();
  }

  public TaskResultOe() {}

  public TaskResultOe(TaskResultEvent evt, String nodeName) {
    this.nodeName = nodeName;

    threadRunNumber = evt.threadRunNumber;
    taskRunNumber = evt.taskRunNumber;
    taskRunPosition = evt.taskRunPosition;

    resultCode = evt.resultCode;
    output = evt.stdout;
    logOutput = evt.stderr;
  }

  public static TaskResultOe fromProto(TaskResultOePb proto) {
    TaskResultOe out = new TaskResultOe();
    out.initFrom(proto);
    return out;
  }
}
