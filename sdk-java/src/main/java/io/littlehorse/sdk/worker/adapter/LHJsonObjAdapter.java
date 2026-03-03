package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public abstract class LHJsonObjAdapter<T> implements LHTypeAdapter<T> {
  public abstract String toJsonObj(T src);
  public abstract T fromJsonObj(String src);

  public VariableType getVariableType() {
    return VariableType.JSON_OBJ;
  }
}
