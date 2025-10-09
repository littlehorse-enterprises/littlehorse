# Checkpointed (Durable) Tasks

This proposal adds support for lightweight durable execution within a single `TaskRun`. It is motivated by AI Agents but has several other practical applications.

## Background: Agents

An agent executes a process that isn't known beforehand. Running an agent task takes a long time, sometimes up to a couple minutes. Putting it in a single TaskRun is not ideal because:
- You can't see the progress and intermediate steps / things it calls to (eg. see the A2A protocol).
- If it fails you need to start from scratch.
- You end up doing side effects multiple times.

### Ideal Case

When an agent does something, like call another agent using A2A protocol or make a database query, we want to be able to "checkpoint" it so that:
1. We can see that the action happened
2. If the agent crashes, we can skip over that side effect the second time we run it

We need a "multi-stage Task" which has the ability to:
1. Send a checkpoint back to the server
   a. Can observe the checkpoint data in the dashboard
   b. If no checkpoint in a certain timeout period, server restarts the task
2. When restarted, it resumes from the previous checkpoint

But Wait! Isn't that just **DURABLE EXECUTION**?

### In LittleHorse

LittleHorse is an open-source project, with many users, and adding a dedicated "Agent Task" without thinking of other solutions is a really bad idea. We need to make it something generic with other use cases. Some other cases include:
* Multi-step document processing on a single machine.
* Long mathematical calculations.
* Training an LLM or something like that.
* Long-running polling tasks (so that you don't need a loop at the `WfSpec` level).
* Image generation pipelines or compute / waiting-intensive tasks.

## Proposal: Extend `TaskRun` Capabilities

The biggest argument for extending TaskRun rather than creating a new Node Type is that the following WfSPec code:

```java
wf.execute("some-task");
```

wouldn't have to change if the implementer of some-task decided to go from a classic task to a multi-step task (i.e. if they decided to go from just RAG to a full-on agent that requires checkpointing). So long as the API contract of the task is unchanged, it's fine.

### Protobuf

```protobuf
message CheckpointId {
  TaskRunId task_run = 1;
  int16 checkpoint_number = 2;
}

// Checkpoint is a Getable
message Checkpoint {
  CheckpointId id = 1;
  VariableValue value = 2;
  optional string logs = 3;
}

message ScheduledTask {
  // all the previous stuff

  // Allows inferring all of the checkpoints. There can be a lot, so in order
  // to keep the `ScheduledTask` lean we do not put it there. This allows tasks
  // to process a lot of data if broken into chunks.
  int16 total_previous_checkpoints = 13;
}

// Used internally by Task Workers to create a `Checkpoint` during the execution of
// a `TaskRun`.
//
// Creates a checkpoint. If the associated `TaskRun` is not found, it returns
// `INVALID_ARGUMENT`. If the associated `TaskRun` is found but is not in a valid
// state (i.e. the `TASK_ATTEMPT` related to this request is not `TASK_RUNNING`),
// then the request returns code `OK` and a `STOP_TASK` value for the field
// `flow_control_continue_type`.
rpc PutCheckpoint(PutCheckpointRequest) returns (PutCheckpointResponse) {}

// Gets a specified `Checkpoint`.
rpc GetCheckpoint(CheckpointId) returns (Checkpoint) {}

// Message to create a Checkpoint.
message PutCheckpointRequest {
  // The id of the `TaskRun` that this `Checkpoint` is associated with.
  TaskRunId task_run_id = 1;

  // The `TaskAttempt` that this checkpoint originates from. This is used to fence
  // zombies. If the checkpointed `TaskAttempt` is not `TASK_RUNNING`, the RPC will
  // return a `STOP_TASK` response.
  int16 task_attempt = 2;

  // The value of the checkpoint.
  VariableValue value = 3;

  // Any user-friendly logs which can be used for debugging.
  optional string logs = 4;
}

// The response for creating a Checkpoint. Used internally by the Task Worker.
message PutCheckpointResponse {
  // Enum used 
  enum FlowControlContinue {
    CONTINUE_TASK = 0;
    STOP_TASK = 1;
  }

  FlowControlContinue flow_control_continue_type = 1;

  optional Checkpoint created_checkpoint = 2;
}
```

### SDK-Level Changes

The SDK would look as follows:

```
@LHTaskMethod("my-agent-task")
void agent(String input, WorkerContext context) {
  String firstCheckpoint = context.executeAndCheckpoint(() -> {
     return foo();
  });

  doSomethingElse(firstCheckpoint);
}
```

The `executeAndCheckpoint()` call would make an `rpc PutCheckpoint`. If the result is `STOP_TASK`, then it would throw and stop the execution of the task method (the Task Worker itself would catch the exception thrown by it).

On a retry `TaskAttempt`, the Task Worker would check to see how many previous `Checkpoint`s there were. Assuming `N` checkpoints (well, let's say `N = 3`), the first three calls to `executeAndCheckpoint()` would not actually execute the lambda function but rather would just fetch the `Checkpoint` from the Server, deserialize the content, and return it.

### Output Topic Changes

The `Checkpoint` will be enabled to be sent to the Output Topic as it is a top-level `Getable`.

## Future Work

The following can all be done in a backwards-compatible way.

### Non-Controversial

* Creating an `rpc ListCheckpoint` which returns all of the Checkpoints in a single request. This is tricky because of pagination; however, it will improve performance.
* Some way to track failed checkpoints in the dashboard (For example, fenced workers).
* Metrics about checkpoints. But we still need the metrics ADR.
* Enhanced timeouts, which allow timeouts for the whole end-to-end `TaskRun` and also timeouts between the `Checkpoint`s.

### Controversial

The following require discussion and may or may not make sense to implement, but they will not be part of this proposal:

* Running a `TaskRun` outside of a `WfSpec` via a new RPC: `rpc RunTask`.
* Calling other `WfSpec` functionality from within a `TaskRun`.
* Using `TaskWorkerHeartbeat`s to keep track of and fence long-running `TaskRun`s that have a long time between `Checkpoint`s.

I could see a future in which we evaluate both proposals and decide against both of them; however, in my travels with customers I have seen use-cases for them already.
