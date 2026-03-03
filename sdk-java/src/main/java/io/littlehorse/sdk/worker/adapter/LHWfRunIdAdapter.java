package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;

public abstract class LHWfRunIdAdapter<T> implements LHTypeAdapter<T> {
  public abstract WfRunId toWfRunId(T src);
  public abstract T fromWfRunId(WfRunId src);

  public VariableType getVariableType() {
    return VariableType.WF_RUN_ID;
  }
}
