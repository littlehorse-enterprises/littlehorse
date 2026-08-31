# workflow-minimal

Create a workflow named `probe-workflow-minimal` (R10) whose entrypoint
thread does nothing, and compile it (sdk-java:
`Workflow.newWorkflow(name, wf -> {}).compileWorkflow()`).

Single-variant case: "having a workflow at all" cannot be subtracted from a
workflow, so there is no base fixture. The fixture pins the builder's
automatic behavior: R2 (entrypoint node), R4 (automatic exit), R5
(entrypoint thread name), R6 (workflow defaults), R7 (thread structure) —
see [../../rules.md](../../rules.md).
