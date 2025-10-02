## Running CastingExample

This example demonstrates both automatic and manual type casting in LittleHorse workflows.

### Overview

The casting example shows how LittleHorse handles type conversions between different variable types:


**Automatic Casting (No .cast() needed):**
- `INT` → `DOUBLE` (automatic)
- `INT` → `STR` (automatic)
- `DOUBLE` → `STR` (automatic)
- `BOOL` → `STR` (automatic)
- `INT` → `DOUBLE` (automatic)
- `INT` → `STR` (automatic)
- `DOUBLE` → `STR` (automatic)
- `BOOL` → `STR` (automatic)

**Manual Casting (Requires .cast() calls):**
- `STR` → `DOUBLE` (manual)
- `DOUBLE` → `INT` (manual)
- `STR` → `BOOL` (manual)
- JSON path result → `INT` (manual, when type is ambiguous)

For more details on casting and type system proposals, see the [proposals directory](../../../proposals/) in the LittleHorse repo.

The workflow demonstrates these casting behaviors through task chaining, variable assignments, and different scenarios where casting occurs.


## Build lhctl from the root of the project
```bash
 cd ./lhctl && go build -o lhctl
```

### Running the Example from the root of the project

```bash
./gradlew example-casting:run
```


In another terminal, use `lhctl` to run the workflow:

```bash
# Run the casting workflow with default values
lhctl run casting-workflow

```

### Inspecting the Results

You can inspect how casting works by examining the workflow specification and execution results:

```bash
# Get the WfSpec to see casting information
lhctl get wfSpec casting-workflow

# Get workflow run details to see casting in action
lhctl get wfRun <wf_run_id>

```
