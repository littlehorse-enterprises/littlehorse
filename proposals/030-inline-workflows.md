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

    // Must be true when selected. Fetch the definition using this WfRun's id.
    bool is_inline = 16;
  }

  // Other existing fields are unchanged.
}
```

### `InlineWfSpec`

```protobuf
message InlineWfSpec {
  // Owning WfRun ID. Output only; omit from RunInlineWf requests.
  WfRunId id = 1;

  // Output only; omit from RunInlineWf requests.
  google.protobuf.Timestamp created_at = 2;

  map<string, ThreadSpec> thread_specs = 3;
  string entrypoint_thread_name = 4;
  // Optional retention policy to clean up the resulting WfRun.
  optional WorkflowRetentionPolicy retention_policy = 5;
}
```

The `InlineWfSpec` uses the same thread graph, entrypoint, and metadata references as a `WfSpec`.

The server validates the thread graph, entrypoint, metadata references, and input variables before accepting the run. Referenced metadata, such as TaskDefs and named StructDefs, must still be registered.

### Identity and lifecycle

The owning `WfRunId` identifies the inline definition and determines its partition. There is no public `InlineWfSpecId` protobuf; `InlineWfSpecIdModel` is an internal server storage identifier.

The server stores an immutable, normalized snapshot separately from, but co-partitioned with, the WfRun. Subsequent commands resolve that snapshot through the run's ID. The definition cannot be independently created or updated and is deleted with its owning WfRun, including retention-based cleanup.

### `rpc RunInlineWf`

Creates a new `WfRun` from an InlineWfSpec. Clients can provide an ID for the run, as with `RunWf`, or let the server generate one.

```protobuf
message RunInlineWfRequest {
  // Supply only thread_specs, entrypoint_thread_name, and retention_policy.
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

Clients can provide variables to the entrypoint thread or use literal values in the inline spec.

Literal values embedded in an `InlineWfSpec`, including variable defaults, are visible through `GetInlineWfSpec`. Marking a workflow variable or TaskDef input as masked does not redact a literal embedded in the definition. For sensitive inputs, declare a masked workflow variable, reference it in the inline graph, and supply its value through `RunInlineWfRequest.variables`. Do not embed the sensitive value as a literal or default in the inline definition.

Missing or invalid definitions and invalid input variables return `INVALID_ARGUMENT`. Supplying the server-managed `InlineWfSpec.id` or `created_at` also returns `INVALID_ARGUMENT`. A caller-provided run ID must satisfy the server's name validation; reusing an existing run ID returns `ALREADY_EXISTS`, rather than returning the existing run.

This RPC requires both `RUN` and `WRITE_METADATA` actions on `ACL_WORKFLOW`.

### `GetInlineWfSpec`

Clients can retrieve the InlineWfSpec using the owning WfRun's ID.

```protobuf
service LittleHorse {
  // Existing RPCs omitted.
  rpc GetInlineWfSpec(WfRunId) returns (InlineWfSpec) {}
}
```

This RPC requires `READ` on `ACL_WORKFLOW`. An empty run ID returns `INVALID_ARGUMENT`. A registered run or a run whose inline definition has been deleted has no inline definition to retrieve and returns `NOT_FOUND`.

### Runtime records and restrictions

Inline runs and their execution records, including ThreadRuns, NodeRuns, Variables, and task-run source references, do not carry a registered `wf_spec_id`. Consumers must resolve the workflow definition through the owning WfRun instead of assuming that field is present. WfSpec-based indexes do not apply to inline runs.

Inline WfRuns cannot be migrated to another workflow definition. Applying a workflow migration plan to an inline run returns `FAILED_PRECONDITION`.

## SDK Changes and Developer Experience

Developers can define an inline workflow using the SDK's workflow-building API, compile it into a `RunInlineWfRequest`, and invoke the `rpc runInlineWf` directly to the server.

The Java SDK provides:

```java
RunInlineWfRequest runInline = Workflow.inlineWorkflow(thread -> thread.execute("hello"))
        .compileWorkflow();
client.runInlineWf(runInline);
```
The returned `InlineWorkflow` builder supports `withWfRunId(...)`, `withRetentionPolicy(...)`, and `compileWorkflow(config)` for configured type adapters. `Workflow.inlineWorkflow(...)` also accepts a placeholder-values map. Input variables can be added to the compiled request with its protobuf builder; task arguments can instead be literals.

The Go SDK provides the equivalent builder:

```go
request, err := littlehorse.NewInlineWorkflow(func(thread *littlehorse.WorkflowThread) {
    output := thread.Execute("hello")
    thread.Complete(output)
}).Compile()
if err != nil {
    return err
}
run, err := client.RunInlineWf(ctx, request)
```

The Go builder supports `WithWfRunID(...)` and `WithRetentionPolicy(...)`. `Compile()` performs no server calls, and inputs can be set on the returned request's `Variables` map.

Java and Go have dedicated inline-workflow builders. Equivalent builders for the other SDKs remain future work, distinct from generated protobuf/RPC support. The normal workflow-building APIs can express multi-step orchestration, task-output dependencies, child threads, events, and failure handling inside an inline workflow.

## `lhctl`

`lhctl execute task` executes a registered TaskDef without registering a WfSpec. It uses the Go inline-workflow builder to create a `RunInlineWf` request containing one task invocation.

Supply positional values in TaskDef input order:

```sh
lhctl execute task my-task Ada 2
```

Alternatively, use repeated `--arg name:value` flags in any order:

```sh
lhctl execute task my-task --arg age:2 --arg name:Ada
```

The CLI resolves types from the TaskDef's `TypeDefinition`s. Positional and named forms cannot be mixed. Omitted inputs use TaskDef defaults; positional arguments can omit only trailing inputs. Unknown, duplicate, missing required, and invalidly typed arguments are rejected.

Struct inputs are JSON objects. The CLI resolves referenced StructDefs and converts the JSON into a typed struct literal:

```sh
lhctl execute task inline-greet-person '{"name":"Ada","age":36}'
lhctl execute task inline-greet-person --arg 'person:{"name":"Ada","age":36}'
```

Quote JSON and values containing spaces. Use `--` before positional values beginning with a dash. Both argument forms support `--wfRunId` to specify the run ID.

Arguments and defaults are embedded as literal task inputs, not declared as workflow variables. For non-void tasks, the task result becomes the workflow output. A task failure fails the workflow.

Consequently, `lhctl execute task` arguments remain visible through `GetInlineWfSpec`, even when the corresponding TaskDef input is masked. For sensitive inputs, use an SDK to declare masked workflow variables and supply their values through `RunInlineWfRequest.variables` instead of this command.
The command prints the returned WfRun immediately, without waiting for completion. TaskDefs and referenced metadata must already be registered, and compatible workers must be running to execute the tasks.

### Future CLI work

Multiple-task invocations, sequential task lists, `--task`, and `--parallel` are not implemented. Clients can use the SDKs to express multiple tasks, parallel execution, and task-output dependencies today.

## Dashboard

The dashboard checks the WfRun's definition source and fetches either the registered WfSpec or the InlineWfSpec using the WfRun ID. The run page renders the inline workflow graph and execution state, displays “Inline workflow,” and avoids assuming a registered WfSpec link is available.
