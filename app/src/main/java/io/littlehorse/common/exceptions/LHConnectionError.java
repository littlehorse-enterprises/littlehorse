package io.littlehorse.common.exceptions;

public class LHConnectionError extends LHException {

  public LHConnectionError(Exception exn, String msg) {
    super(exn, msg);
  }
}
