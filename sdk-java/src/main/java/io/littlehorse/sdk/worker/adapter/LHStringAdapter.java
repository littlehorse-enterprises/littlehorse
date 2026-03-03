package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public interface LHStringAdapter<T> extends LHTypeAdapter<T> {
  public String toString(T src);
  public T fromString(String src);

  default public VariableType getVariableType() {
    return VariableType.STR;
  }
}
