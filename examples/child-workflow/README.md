## Running `ChildWorkflowExample`

This is a basic example of two `WfSpec`s, `parent` and `child`, in which the `child` `WfSpec` uses a `Variable` defined from the `parent`.

First, run the application. This creates both `WfSpec`'s and registers a Task Worker.
```
./gradlew example-child-workflow:run
```

In another terminal, use `lhctl` to run the parent workflow:

```
lhctl run parent name obi-wan
```

Next, run the child:

```
lhctl run child --parentWfRunId <id of previous WfRun>
```

You can use `lhctl get wfRun` to inspect the parent's result.
