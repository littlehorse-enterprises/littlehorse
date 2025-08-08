## Running CastingExample

This example demonstrates automatic type casting in LittleHorse. The workflow shows how primitive types are automatically converted when passed to tasks expecting different types.

**Automatic Casting Rules Demonstrated:**
- `INT` → `STR` (automatic)
- `INT` → `DOUBLE` (automatic)  
- `DOUBLE` → `STR` (automatic)
- `BOOL` → `STR` (automatic)

The example includes both literal values and variables being automatically cast to the expected task parameter types.

Let's run the example in `CastingExample.java`

```bash
./gradlew example-casting:run
```

In another terminal, use `lhctl` to run the workflow:

```bash
# Run the casting workflow with automatic conversions
lhctl run example-casting

# You can also provide values for the variables (these will be cast automatically)
lhctl run example-casting int-var 42 double-var 3.14 bool-var true
```

In addition, you can check the result with:

```bash
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information and the automatically cast values
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
