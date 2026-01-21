package io.littlehorse.examples;

import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnyOfWorker {

  private static final Logger log = LoggerFactory.getLogger(AnyOfWorker.class);
  private final LittleHorseBlockingStub client;


  public AnyOfWorker(LittleHorseBlockingStub client) {
    this.client = client;
  }


  @LHTaskMethod("confirm-result")
  public String confirm() {
    log.debug("Executing confirm");
    return "done";
  }


  @LHTaskMethod("send-interrupt")
  public void sendInterruptTask(WorkerContext workerContext) {
    client.putExternalEvent(PutExternalEventRequest.newBuilder()
        .setWfRunId(workerContext.getWfRunId())
        .setExternalEventDefId(ExternalEventDefId.newBuilder().setName("interrupt").build())
        .build());

  }

}
