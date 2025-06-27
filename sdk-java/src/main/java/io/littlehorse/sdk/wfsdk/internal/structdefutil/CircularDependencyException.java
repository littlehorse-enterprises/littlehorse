package io.littlehorse.sdk.wfsdk.internal.structdefutil;

public class CircularDependencyException extends RuntimeException {
  public CircularDependencyException(String message) {
    super(message);
  }
}
