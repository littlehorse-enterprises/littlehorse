## Running ChildThreadExample

This is a simple example which shows the usage of checkpoints in tasks.

Let's run the example in `CheckpointTasksExample`

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-checkpointed-tasks input-name "Qui-Gon Jinn"
```

Observe the task worker output to see what's going on.