package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public abstract class LHLongAdapter<T> implements LHTypeAdapter<T> {
  public abstract Long toLong(T src);
  public abstract T fromLong(Long src);

  public VariableType getVariableType() {
    return VariableType.INT;
  }
  
}
