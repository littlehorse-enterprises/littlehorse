## MapExample: a typed-Map inventory workflow

This example uses a typed Map to reserve inventory. The workflow:

- checks the available stock for the requested SKU
- reserves the order when enough stock is available
- updates the SKU's quantity in the inventory Map
- sends the reservation and updated inventory to task workers
- notifies a worker when the order cannot be fulfilled

Start the workers and register the `reserve-inventory` workflow:

```
./gradlew example-maps:run
```

In another terminal, start a workflow run with `lhctl`.

The `inventory` input is a typed `Map<STR, INT>`. This run reserves two apples from
the default inventory and saves the remaining quantity:

```
lhctl run reserve-inventory
```

Choose a SKU and quantity:

```
lhctl run reserve-inventory sku bananas quantity 4
```

Or provide the inventory snapshot for this reservation:

```
lhctl run reserve-inventory \
  inventory '{"coffee": 20, "tea": 8}' \
  sku coffee \
  quantity 6
```

Request more than is available to exercise the out-of-stock path:

```
lhctl run reserve-inventory sku apples quantity 10
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
