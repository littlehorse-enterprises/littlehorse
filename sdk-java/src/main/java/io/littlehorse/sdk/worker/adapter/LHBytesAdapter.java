package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public abstract class LHBytesAdapter<T> implements LHTypeAdapter<T> {
  public abstract byte[] toBytes(T src);
  public abstract T fromBytes(byte[] src);

  public VariableType getVariableType() {
    return VariableType.BYTES;
  }
  
}
