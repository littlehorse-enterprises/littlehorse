# Run Child Workflow (Go)

This example shows how to spawn a child Workflow (WfSpec) from a parent workflow and wait for its completion using the Go SDK convenience APIs `RunWf` and `WaitForChildWf`.

Steps

1. Start the task workers:

```
go run ./examples/go/run-child-workflow/worker
```

2. Register the WfSpecs (child then parent):

```
go run ./examples/go/run-child-workflow/deploy
```

3. Run the parent workflow:

```
lhctl run run-child-workflow-parent name "value-for-child"
```

4. Inspect the WfRun and look for the child WfRun created by the RunChildWf node.
