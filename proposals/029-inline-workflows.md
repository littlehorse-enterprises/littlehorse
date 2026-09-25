# Inline Workflow Runs

**Author:** Eduwer Camacaro

## Context

Today, executing a task method through LittleHorse requires a registered `TaskDef` and a registered `WfSpec` that invokes it.
This is foundational to LittleHorse and works well for reusable business processes.
However, some executions require orchestration without requiring clients to manage, distribute, and version a separate workflow definition.
Making `WfSpec` registration mandatory adds friction in cases where developers want to invoke a task method with specific inputs and inspect its results while debugging.

This proposal aims to allow clients to define and run workflows without registering a WfSpec.

## Server API Changes


### `WfRun`

The key change is to add a spec source for WfRuns. The source could be either a registered `WfSpec` or an inline `WfSpec` that was provided at invocation time.

```protobuf
message WfRun {
  WfRunId id = 1;

  oneof wf_spec_source {
    WfSpecId wf_spec_id = 2;

    InlineWfSpecId inline_wf_spec_id = 15;
  }

  // Other existing fields are unchanged.
}
```

### `InlineWfSpec`


```protobuf
message InlineWfSpec {
  // Identity for the one-off workflow
  InlineWfSpecId id = 1;

  google.protobuf.Timestamp created_at = 2;

  map<string, ThreadSpec> thread_specs = 3;
  string entrypoint_thread_name = 4;
  // Optional retention policy to clean up the resuling WfRun
  optional WorkflowRetentionPolicy retention_policy = 5;
}
```

The `InlineWfSpec` has a similar structure to a `WfSpec` and uses the same thread graph, entrypoint, and metadata references

The server validates the thread graph, entrypoint, metadata references, and input variables before accepting the run just like any other `WfSpec`

### `InlineWfSpecId`

Defines both identity and partition key for the inline-spec. The `InlineWfSpec` is co-partitioned with the owning `WfRun` record.

```protobuf
message InlineWfSpecId {
  WfRunId wf_run_id = 1;
}
```

### `rpc RunInlineWf`

Creates a new `WfRun` from an InlineWfSpec. Clients could also provide an ID for the run similar to the `rpc RunWf`.
The InlineWfSpec will inherent the same validations as a `WfSpec`.

```protobuf
message RunInlineWfRequest {
  InlineWfSpec wf_spec = 1;

  // Inputs to the entrypoint ThreadRun.
  map<string, VariableValue> variables = 2;

  // Optional caller-provided WfRun ID.
  optional string id = 3;
}

service LittleHorse {
  
  rpc RunInlineWf(RunInlineWfRequest) returns (WfRun) {}
  
}
```

Clients can optionally provide variables to the entrypoint thread, or alternatively, they can use literal value on the inline spec.

### `GetInlineWfSpec`

Clients can retrive the InlineWfSpec for a given WfRun.
```protobuf
service LittleHorse {
  // Existing RPCs omitted.
  rpc GetInlineWfSpec(InlineWfSpecId) returns (InlineWfSpec) {}
}
```

## SDK Changes and Developer Experience

Developers can define an inline workflow using the SDK's workflow-building API, compile it into a `RunInlineWfRequest`, and invoke the `rpc runInlineWf` directly to the server.

In the Java SDK, the proposed API is:

```java
RunInlineWfRequest runInline = Workflow.inlineWorkflow(thread -> thread.execute("hello"))
        .compileWorkflow();
client.runInlineWf(runInline);
```
Equivalent functionality will be provided in the non-Java SDKs, following each language's conventions.

## `lhctl`

`lhctl` piggy backs on this feature to provide a CLI command for executing task methods without registering a WfSpec.
`lhctl` transforms the `lhctl run tasks` command into a `RunInlineWf` request.

```sh
lhctl run tasks hello name Alice
```

For multiple tasks, each `--task` starts a task invocation followed by its input name/value pairs. Tasks execute sequentially in the supplied order by default:

```sh
lhctl run tasks \
  --task hello name Alice \
  --task send-email recipient alice@example.com subject Welcome
```

Adding `--parallel` executes the tasks concurrently:

```sh
lhctl run tasks --parallel \
  --task hello name Alice \
  --task send-email recipient alice@example.com subject Welcome
```

The single-task positional form is shorthand for `lhctl run tasks --task hello name Alice`. Both forms support `--wfRunId` to specify the run ID.

It prints the returned WfRun immediately, without waiting for WfRun completion. TaskDefs must already be registered.

- **Sequential:** the workflow executes each task in order within one thread. A task failure fails the workflow.
- **Parallel:** the workflow starts one child thread per task and waits for all children.

Passing one task's output into another task is outside the initial CLI API; clients can use the SDK to express those dependencies.