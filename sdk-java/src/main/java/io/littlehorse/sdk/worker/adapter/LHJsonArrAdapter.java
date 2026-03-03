package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public abstract class LHJsonArrAdapter<T> implements LHTypeAdapter<T> {
  public abstract String toJsonArr(T src);
  public abstract T fromJsonArr(String src);

  public VariableType getVariableType() {
    return VariableType.JSON_ARR;
  }
}
