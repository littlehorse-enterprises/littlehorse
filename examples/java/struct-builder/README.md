## Running StructBuilder Example [Experimental]

This example demonstrates building `Struct` values **inside a WfSpec** using the `LHStructBuilder` and `InlineLHStructBuilder` APIs.

Rather than relying on a task to return a fully-formed `Struct`, this workflow assembles a `Person` struct by combining input variables (`name`, `email`) with the output of a `fetch-address` task, all within the workflow definition itself.

### Key Concepts

- `wf.buildStruct("person")` creates an `LHStructBuilder` tied to the `person` StructDef.
- `.put("fieldName", value)` sets each field using workflow variables, task outputs, or literals.
- `wf.buildInlineStruct()` creates an `InlineLHStructBuilder` for nested sub-structures (like `address` inside `person`).
- The inline builder is **not** `Serializable`, so it can only be used inside another builder -- it cannot be accidentally passed to `execute()` or `assign()`.

### Workflow Overview

```
Input: name (STR), email (STR)
  |
  v
[fetch-address] -- returns an Address struct by name
  |
  v
Build "person" struct using buildStruct:
  - name  <- input variable
  - email <- input variable
  - address <- buildInlineStruct from fetch-address output fields
  |
  v
[save-person] -- receives the assembled Person struct
```

### Register and Start Workers

```
./gradlew example-struct-builder:run
```

This will:
1. Register the `address` and `person` `StructDef`s
2. Register the `fetch-address` and `save-person` `TaskDef`s
3. Register the `assemble-person` `WfSpec`
4. Start the task workers

### Run a Workflow

In a separate terminal:

```
./gradlew example-struct-builder:run --args 'Obi-Wan obi-wan@jedi.org'
```

### Inspect Results

```
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
