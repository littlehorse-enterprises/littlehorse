package io.littlehorse.examples;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import java.util.UUID;

public class UUIDTypeAdapter implements LHStringAdapter<UUID> {
  @Override
  public String toString(UUID src) {
    return src.toString();
  }

  @Override
  public UUID fromString(String src) {
    return UUID.fromString(src);
  }

  @Override
  public Class<UUID> getTypeClass() {
    return UUID.class;
  }
}
