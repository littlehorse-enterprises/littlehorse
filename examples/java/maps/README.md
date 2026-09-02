## MapExample: a typed-Map inventory workflow

This example models a small **inventory** (`item -> quantity`) and showcases the four core
LittleHorse typed-Map features:

1. **Map input to a Task** — `summarize-inventory` receives the whole `Map` and returns the total
   quantity on hand.
2. **`map.get(key)` in the DSL** — reads a single entry by a *literal* key
   (`inventory.get("apples")`) into an `INT` variable.
3. **`map.doesContain(key)`** — branches with `doIfElse` on whether a runtime `lookup-key` exists.
4. **A Map WfRunVariable** — `my-map` is declared as `Map<STR, INT>` with a default, then mutated
   in-place by merging the `restock` Task output (`inventory.extend(...)`).

> Note: the DSL `map.get(...)` requires a compile-time key. When the key is only known at runtime
> (the `lookup-key` variable), the lookup is done inside the `get-quantity` Task, which receives the
> whole Map plus the key.

## Running

Start the example Java app (registers the workflow + task workers):

```
./gradlew example-maps:run
```

In another terminal, start workflow runs with `lhctl`.

```
# Uses the default inventory: {"apples": 3, "bananas": 5, "cherries": 12}
# lookup-key defaults to "apples", so the get-quantity branch runs.
lhctl run example-maps
```

The `my-map` input variable is a typed `Map<STR, INT>`. Provide it as a JSON object; keys and
values are coerced to the declared key/value types. Override `lookup-key` to pick a different item:

```
# Provide your own inventory and look up "grapes"
lhctl run example-maps my-map '{"apples": 10, "grapes": 7}' lookup-key grapes

# Key "apples" (default lookup-key) is absent -> report-missing branch runs
lhctl run example-maps my-map '{"jacob": 5}'
```

## Inspecting a run

```
# Show the workflow run and its variables (my-map, total-count, apples-qty, ...)
lhctl get wfRun <wf_run_id>

# List all node runs for the workflow
lhctl list nodeRun <wf_run_id>

# Show task run details
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
