## Running CastingExample

This example demonstrates both automatic and manual type casting in LittleHorse workflows.

### Overview

The casting example shows how LittleHorse handles type conversions between different variable types:

**Automatic Casting (No .cast() needed):**
- `INT` → `DOUBLE` (automatic)
- `DOUBLE` → `STR` (automatic)
- `BOOL` → `STR` (automatic)
- All primitive types → `STR` (automatic)

**Manual Casting (Requires .cast() calls):**
- `STR` → `INT`/`DOUBLE`/`BOOL` (manual)
- `DOUBLE` → `INT` (manual)

The workflow demonstrates these casting behaviors through task chaining, variable assignments, and different scenarios where casting occurs.

### Running the Example

```bash
./gradlew example-casting:run
```

In another terminal, use `lhctl` to run the workflow:

```bash
# Run the casting workflow with default values
lhctl run casting-workflow

# Run with custom input values
lhctl run casting-workflow string-var "false" int-var 50
```

### Inspecting the Results

You can inspect how casting works by examining the workflow specification and execution results:

```bash
# Get the WfSpec to see casting information
lhctl get wfSpec casting-workflow

# Look for "cast_to" fields in variable assignments
lhctl get wfSpec casting-workflow | jq '.threadSpecs.entrypoint.nodes[] | select(.task) | .task.variables[]'

# Get workflow run details to see casting in action
lhctl get wfRun <wf_run_id>

# List task runs to see input/output values with casting
lhctl list nodeRun <wf_run_id>
```

### Code Formatting

To format the code in this example:

```bash
# Apply code formatting
./gradlew :example-casting:spotlessApply

# Check code formatting
./gradlew :example-casting:spotlessCheck
```
