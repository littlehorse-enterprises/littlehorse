## Running CastingExample

This example demonstrates automatic type casting in LittleHorse. The workflow shows how primitive types are automatically converted when passed to tasks expecting different types.

**Automatic Casting Rules Demonstrated:**
- `INT` → `STR` (automatic)
- `INT` → `DOUBLE` (automatic)  
- `DOUBLE` → `STR` (automatic)
- `BOOL` → `STR` (automatic)

The example includes both literal values and variables being automatically cast to the expected task parameter types. The workflow creates variables of different types and demonstrates casting through:

1. **Literal casting**: Direct values passed to tasks (e.g., `42` → string-method)
2. **Variable casting**: Variables of one type passed to tasks expecting another type
3. **Result casting**: Task results being cast when passed to subsequent tasks

### Running the Example

```bash
./gradlew example-casting:run
```

In another terminal, use `lhctl` to run the workflow:

```bash
# Run the casting workflow with automatic conversions
lhctl run auto-casting-workflow

# You can also provide values for the variables (these will be cast automatically)
lhctl run auto-casting-workflow int-var 42 double-var 3.14 bool-var true string-var "hello"
```

### Checking Results

You can inspect the results to see the automatic casting in action:

```bash
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information and the automatically cast values
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

### Code Formatting

To format the code in this example:

```bash
# Apply code formatting
./gradlew :example-casting:spotlessApply

# Check code formatting
./gradlew :example-casting:spotlessCheck
```
