# cancel-user-task-run-after

Build `probe-cancel-user-task-run-after` (R10) exercising `WorkflowThread#cancelUserTaskRunAfter`.
The base is the nearest do-nothing neighbor; the feature adds exactly the
gesture above, and the fixture diff is its entire effect.
The reference body is in WfsdkArea.java (see rules.md, "Minting"); the
frozen fixtures are the contract. Rules: [../../rules.md](../../rules.md).
