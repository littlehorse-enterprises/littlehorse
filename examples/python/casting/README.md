# Casting Example (Python)

This example demonstrates both automatic and manual type casting in LittleHorse workflows.

## Overview

The casting example shows how LittleHorse handles type conversions between different variable types:

**Automatic Casting** (no cast call required):
- `INT` → `DOUBLE`, `INT` → `STR`, `DOUBLE` → `STR`, `BOOL` → `STR`

**Manual Casting** (requires `cast_to_*()` calls):
- `STR` → `DOUBLE`
- `DOUBLE` → `INT`
- `STR` → `BOOL`
- JSON path result → `INT` (when type is ambiguous)

The workflow demonstrates:
1. Declaring variables with default values (`string-number`, `string-bool`, `json-input`)
2. Manual casting from STR to DOUBLE to INT
3. Math expressions on DOUBLE values and casting results to INT
4. Error handling for invalid casts (e.g., non-boolean string to BOOL)
5. JSON path extraction with explicit type casting

## Running the Example

First, make sure to install/update dependencies (needed after any SDK changes):

```
poetry install
```

Then run the example:

```
poetry shell
python -m example_casting
```

In another terminal, use `lhctl` to run the workflow:

```
# Run with default values
lhctl run casting-workflow

# Run with custom string-number value
lhctl run casting-workflow string-number 5.67

# Run with custom json-input
lhctl run casting-workflow json-input '{"int":"42","string":"world"}'
```

## Checking Results

In addition, you can check the result with:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

## Trigger the Error Handler

To test the error handler, you can try running the workflow with a `string-bool` value that cannot be cast to boolean (e.g., "hello" instead of "true"/"false"). This will trigger the error handler at the bool-method node.

```bash
lhctl run casting-workflow string-bool Hello --wfRunId error-handler
```

To inspect the failed node run:

```bash
lhctl get nodeRun error-handler 0 4
```
