## MOTIVATION
LittleHorse creates events for state transitions of `UserTaskRuns`, such as when they are assigned `UTEAssigned`, canceled `UTECancelled`, saved `UTESaved`, or scheduled `UTETaskExecuted`. However, there is no UTE that currently represents when a `UserTask` has been completed. 

Introducing `UTECompleted` ensures that all critical state transitions of `UserTasks` are fully represented and observable within the audit log. This allows for:

- Visibility into when a `userTask` has been completed.

- Adding `UTECompleted` will complete the audit log for `UserTaskRun` state changes, ensuring that the event log fully captures the complete source of truth for the lifecycle of a `UserTaskRun`.

## Proposed Protocol Buffer changes

Add new message:
```proto
message UserTaskEvent {
  //...

  // Message denoting that the UserTaskRun has been completed
  message UTECompleted {
    // The ID of the TaskRun that was just completed
    TaskRunId task_run_id = 1;
  }
  ...
}
```

Add new message as oneof:
```proto
message UserTaskEvent {

  // The event that occurred.
  oneof event {
    //...
    // This event will log that the UserTaskRun has been completed
    UTECompleted completed = 6;
  }
}
```
## Backwards Compatibility
This proposal is fully backward compatible. It introduces a new message `UTECompleted` and adds one new field to the existing oneof event.