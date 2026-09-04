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

In another terminal, start a workflow run with `lhctl`. The workflow loads its typed
`Map<STR, INT>` inventory from the `get-inventory` task, then reserves two apples and
saves the remaining quantity:

```
lhctl run reserve-inventory
```

Choose an item and quantity:

```
lhctl run reserve-inventory item-to-reserve bananas quantity-to-reserve 4
```

Request more than is available to exercise the out-of-stock path:

```
lhctl run reserve-inventory item-to-reserve apples quantity-to-reserve 10
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
