package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Simple demonstration of a workflow that demonstrates waiting on any one of three external events before
 * proceeding with the main flow.
 */
public class AnyOfExample {

  private static final Logger log = LoggerFactory.getLogger(AnyOfExample.class);


  // A generic interrupt handler which fails if the parent threads activated variable is not set
  public static ThreadFunc getInterruptHandler(WfRunVariable activated) {
    return wf -> {
      wf.doIf(
          wf.condition(activated, Comparator.EQUALS, Boolean.TRUE),
          ifThread -> wf.fail("complete", "complete")
      );
    };
  }


  // A thread which waits for the given external eventName or is interrupted
  public static ThreadFunc getWaitingThread(String eventName) {
    return wf -> {
      //Prepare anyOf pattern
      //TODO - This should? work according to documentation, but errors at server
      WfRunVariable activated = wf.declareBool("activated" + eventName);


      //TODO - This causes error in the API, but according to code documentation in validateExternalEventDefUse()
//     * 2. An ExternalEventDef CAN be used as an Interrupt trigger in more
//          * than one ThreadSpec.

      wf.registerInterruptHandler("interrupt", getInterruptHandler(activated));

      //Wait for the event
      wf.waitForEvent(eventName);

      //If the winner, notify others
      wf.execute("send-interrupt");
    };
  }


  public static Workflow getWorkflow() {
    return new WorkflowImpl(
        "any-of-example", wf -> {
      WfRunVariable parentVar = wf.declareInt("parent-var");

      //Wait for anyOf these events
      WaitForThreadsNodeOutput result = wf.waitForThreads(
          SpawnedThreads.of(
              wf.spawnThread(getWaitingThread("event-a"), "eventAListener", null),
              wf.spawnThread(getWaitingThread("event-b"), "eventBListener", null),
              wf.spawnThread(getWaitingThread("event-c"), "eventCListener", null)
          )
      );

      //Now determine which event happened
      wf.execute("confirm-result");

    }
    );
  }


  public static Properties getConfigProps() throws IOException {
    Properties props = new Properties();
    File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
        .toFile();
    if (configPath.exists()) {
      props.load(new FileInputStream(configPath));
    }
    return props;
  }


  public static List<LHTaskWorker> getTaskWorkers(LHConfig config, LittleHorseBlockingStub client) {
    AnyOfWorker executable = new AnyOfWorker(client);
    List<LHTaskWorker> workers =
        List.of(new LHTaskWorker(executable, "confirm-result", config), new LHTaskWorker(executable, "send-interrupt", config));

    // Gracefully shutdown
    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
          log.debug("Closing {}", worker.getTaskDefName());
          worker.close();
        })));
    return workers;
  }


  public static void main(String[] args) throws IOException {
    // Let's prepare the configurations
    Properties props = getConfigProps();
    LHConfig config = new LHConfig(props);
    LittleHorseBlockingStub client = config.getBlockingStub();

    // New workflow
    Workflow workflow = getWorkflow();

    // New worker
    List<LHTaskWorker> workers = getTaskWorkers(config, client);

    // Register tasks
    for (LHTaskWorker worker : workers) {
      worker.registerTaskDef();
    }

    // Register events
    client.putExternalEventDef(PutExternalEventDefRequest.newBuilder().setName("interrupt").build());
    client.putExternalEventDef(PutExternalEventDefRequest.newBuilder().setName("event-a").build());
    client.putExternalEventDef(PutExternalEventDefRequest.newBuilder().setName("event-b").build());
    client.putExternalEventDef(PutExternalEventDefRequest.newBuilder().setName("event-c").build());

    // Register a workflow
    workflow.registerWfSpec(client);

    // Run the workers
    for (LHTaskWorker worker : workers) {
      log.debug("Starting {}", worker.getTaskDefName());
      worker.start();
    }
  }

}
